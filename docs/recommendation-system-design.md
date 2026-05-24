# Sistema de Recomendación Híbrido — Documentación de Diseño

## Resumen Ejecutivo

Se implementó un **sistema de recomendación híbrido** para la plataforma SelecTA (gestión de asignaturas universitarias UPM). El sistema combina 5 señales de recomendación con pesos adaptativos que resuelven el problema de cold-start, y aplica re-ranking MMR para garantizar diversidad en los resultados.

---

## 1. Fórmula de Scoring

```
Score(subject) = w1 × TagAffinity + w2 × Collaborative + w3 × Popularity + w4 × ContentMatch + w5 × Diversity
```

Cada señal está normalizada en el rango [0, 1], y los pesos suman 1.0, por lo que el score final también queda en [0, 1].

---

## 2. Señales Implementadas

### 2.1 Tag Affinity (Jaccard Ponderado)

**Qué hace:** Mide la afinidad temática entre el perfil del usuario y una asignatura candidata.

**Algoritmo elegido:** Weighted Jaccard Similarity

```
TagAffinity = Σ min(w_profile(t), w_subject(t)) / Σ max(w_profile(t), w_subject(t))
```

Donde:
- `w_profile(t)` = peso normalizado del tag `t` en el perfil del usuario (derivado de ratings ≥4★, con más peso para ratings de 5★)
- `w_subject(t)` = 1.0 para todos los tags de la asignatura candidata

**Decisiones de diseño:**

| Alternativa considerada | Por qué se descartó |
|-------------------------|---------------------|
| Jaccard puro (sin pesos) | Trata todos los tags por igual; un usuario que ha dado 5★ a 3 asignaturas de "ML" y 4★ a 1 de "Redes" debería tener más afinidad con ML |
| Cosine similarity sobre TF-IDF | Más complejo, requiere corpus de documentos; el dominio universitario es pequeño y el Jaccard ponderado captura la misma intuición |
| Content-Based con embeddings | Sobreingeniería para el tamaño del catálogo (~100 asignaturas); no justificable sin un corpus grande |

**Construcción del perfil:**
- Solo se consideran ratings ≥ 4 como señal positiva de interés
- Los tags se extraen de las asignaturas bien valoradas
- El peso de cada tag es proporcional al rating (`rating/5.0`), normalizado al máximo
- Los favoritos (bookmarks) NO se usan como señal — son marcadores funcionales, no indicadores de preferencia

---

### 2.2 Collaborative Filtering (User-User CF con Cosine Similarity)

**Qué hace:** Identifica usuarios con gustos similares y recomienda lo que a ellos les gustó.

**Algoritmo elegido:** User-User CF simplificado con Cosine Similarity como métrica de vecindario.

**Parámetros:**
- Top-5 vecinos (MAX_NEIGHBORS = 5)
- Mínimo 2 ratings en común para considerar a un usuario como vecino (MIN_COMMON_RATINGS = 2)
- Señal resultante = media ponderada de ratings de vecinos / 5.0

**Proceso:**
1. Encontrar usuarios que han valorado las mismas asignaturas que el usuario actual
2. Calcular Cosine Similarity entre vectores de ratings
3. Seleccionar top-5 vecinos más similares
4. De esos vecinos, agregar sus ratings sobre asignaturas candidatas (que el usuario actual NO ha valorado)
5. Score = media ponderada por similitud, normalizada a [0,1]

**Decisiones de diseño:**

| Alternativa considerada | Por qué se descartó |
|-------------------------|---------------------|
| Pearson Correlation | Requiere ≥3 ratings en común y centra los ratings en la media; con pocos datos, Cosine funciona mejor |
| Item-Item CF | Más estable con muchos usuarios pero pocos items; en nuestro caso el catálogo es pequeño (~100) y los usuarios crecerán, así que User-User escala mejor con el tiempo |
| Matrix Factorization (SVD) | Requiere batch processing y reentrenamiento; no justificable para el tamaño actual del dataset |
| Overlap count simplificado | Demasiado simplista para un TFG; Cosine ofrece rigor matemático sin mucha más complejidad |

**Cold-start handling:** Cuando no hay suficientes vecinos, la señal devuelve 0.0 y el sistema adaptativo reduce su peso a 0.

---

### 2.3 Popularity (Wilson Confidence Score)

**Qué hace:** Estima la calidad de una asignatura combinando su rating medio con un factor de confianza basado en la cantidad de valoraciones.

**Fórmula:**
```
Popularity = (avgRating / 5.0) × (1 - 1/(1 + ln(numRatings + 1)))
```

**Justificación matemática:**
- El término `avgRating/5.0` normaliza la calidad percibida a [0,1]
- El factor `(1 - 1/(1+ln(n+1)))` es una función de confianza que:
  - Con 1 rating → factor ≈ 0.31 (baja confianza)
  - Con 10 ratings → factor ≈ 0.58 (media confianza)
  - Con 50 ratings → factor ≈ 0.74 (alta confianza)
  - Con 100 ratings → factor ≈ 0.78 (convergencia)

**Propiedad clave demostrada en tests:** 1 review de 5★ puntúa MENOS que 50 reviews de 4★.

**Decisiones de diseño:**

| Alternativa considerada | Por qué se descartó |
|-------------------------|---------------------|
| Media aritmética simple | No penaliza asignaturas con 1 sola valoración; sesgo hacia ratings espurios |
| Wilson Score Interval completo (estadístico) | Diseñado para ratings binarios (up/down); requiere adaptación no trivial para ratings 1-5 |
| Bayesian Average | Requiere definir un "prior" (rating medio global y peso); funcional pero menos intuitivo de explicar |
| IMDB Weighted Rating | Similar a Bayesian, usado por IMDB; funciona pero nuestra fórmula es más simple y equivalente en comportamiento |

---

### 2.4 Content Match (Soft Filters)

**Qué hace:** Mide cuánto una asignatura satisface los criterios explícitos del formulario del usuario.

**Fórmula:**
```
ContentMatch = filtros_satisfechos / filtros_activos
```

**Filtros evaluados:**
- Semestre (ODD/EVEN)
- Idioma
- Créditos máximos (ECTS)
- Tags seleccionados (chips del formulario)

**Decisión clave: Soft Filters vs Hard Filters**

| Enfoque | Comportamiento | Decisión |
|---------|---------------|----------|
| **Hard Filters** (excluyentes) | Si marcas "Inglés", solo ves asignaturas en inglés | ❌ Descartado |
| **Soft Filters** (señales) | "Inglés" suma al score pero no excluye asignaturas geniales en español | ✅ Elegido |

**Justificación:** En un sistema de recomendación, es preferible que una asignatura con alta afinidad temática (TagAffinity=0.9) aparezca aunque no cumpla un filtro de idioma, en lugar de excluirla completamente. El usuario ve el % de match y puede decidir.

---

### 2.5 Diversity (MMR — Maximal Marginal Relevance)

**Qué hace:** Garantiza que la lista de recomendaciones no sea monótonamente similar (evita "echo chambers").

**Se implementa en dos niveles:**

#### Nivel 1: Señal individual pre-ranking
```
Diversity(s) = 1 - (Σ profile_weight(tag) para tags en s) / |tags de s|
```
Premia asignaturas que exploran áreas nuevas no cubiertas por el perfil.

#### Nivel 2: MMR Re-ranking (Carbonell & Goldstein, 1998)

Tras el scoring inicial, se aplica re-ranking iterativo greedy:
```
MMR(s) = λ × Relevance(s) - (1-λ) × max_similarity(s, ya_seleccionadas)
```

Con `λ = 0.7` (70% relevancia, 30% diversidad).

**Proceso:**
1. Se selecciona la asignatura con mayor score
2. Para cada siguiente posición, se recalcula el MMR score penalizando similitud con las ya seleccionadas
3. La similitud inter-asignatura se mide con Jaccard entre tags

**Decisiones de diseño:**

| Alternativa considerada | Por qué se descartó |
|-------------------------|---------------------|
| Sin diversidad | Produce listas homogéneas; si te gusta IA, todas las recomendaciones serían de IA |
| Diversidad solo a nivel de perfil | Funciona pero no evita redundancia en la lista final (5 asignaturas casi idénticas) |
| **MMR** | ✅ Elegido — algoritmo con paper de referencia, implementación simple (O(N×K)), resultados demostrados |
| DPP (Determinantal Point Processes) | Más sofisticado pero computacionalmente costoso; excesivo para ~100 candidatos |

**Referencia:** Carbonell, J., & Goldstein, J. (1998). *The Use of MMR, Diversity-Based Reranking for Reordering Documents and Producing Summaries.* SIGIR '98.

---

## 3. Pesos Adaptativos (Cold-Start Handling)

El sistema adapta los pesos según el contexto del usuario:

| Contexto | TagAffinity | Collaborative | Popularity | ContentMatch | Diversity |
|----------|:-----------:|:-------------:|:----------:|:------------:|:---------:|
| Sin login | 0.00 | 0.00 | 0.40 | 0.50 | 0.10 |
| Login sin ratings | 0.00 | 0.00 | 0.35 | 0.50 | 0.15 |
| Login con <3 ratings | 0.25 | 0.10 | 0.30 | 0.25 | 0.10 |
| Login con ≥3 ratings | 0.35 | 0.25 | 0.20 | 0.15 | 0.05 |

**Justificación:**
- **Sin login:** No hay personalización posible → depende de popularidad y filtros explícitos
- **Login sin ratings:** El usuario existe pero no ha expresado preferencias → popularidad + filtros
- **Pocas ratings:** Se empieza a personalizar pero con cautela; el CF aún no es fiable
- **Historial rico:** Personalización completa; se reduce el peso de filtros explícitos porque el sistema ya "entiende" al usuario

**Este diseño resuelve elegantemente el cold-start problem**, uno de los desafíos fundamentales en sistemas de recomendación.

---

## 4. Explicabilidad (Explainable Recommendations)

Cada recomendación incluye una explicación textual basada en la señal dominante:

| Señal dominante | Explicación generada |
|-----------------|---------------------|
| tagAffinity | "Afín a tus intereses en {tags coincidentes}" |
| collaborative | "Estudiantes con gustos similares valoran bien esta asignatura" |
| popularity | "Popular entre estudiantes" |
| contentMatch | "Encaja con tus preferencias de búsqueda" |
| diversity | "Explora un área diferente a tu perfil habitual" |

Además, la UI ofrece un **desglose visual** expandible con barras de progreso por señal.

**Relevancia para el TFG:** La explicabilidad (XAI) en sistemas de recomendación es un tema de investigación activo (Tintarev & Masthoff, 2007; Zhang & Chen, 2020). Permite al usuario entender y confiar en las recomendaciones.

---

## 5. Arquitectura de Implementación

### Clases creadas:

| Clase | Responsabilidad |
|-------|----------------|
| `UserContext` (enum) | Determina el contexto del usuario para selección de pesos |
| `RecommendationWeights` | Encapsula los 5 pesos con factory method por contexto |
| `UserInterestProfile` | Perfil de interés derivado de ratings (tags ponderados, sujetos valorados) |
| `SubjectScoreDTO` | DTO con score, breakdown, explicación y metadatos para la vista |
| `RecommendationEngine` | Servicio principal: orquesta señales, scoring y MMR |

### Clases modificadas:

| Clase | Cambio |
|-------|--------|
| `SubjectRatingRepository` | Nuevas queries: findByUserId, findBySubjectIdIn, findAllWithSubject |
| `SubjectRecommendationCriteria` | Añadido campo `selectedTags` |
| `SubjectRecommenderDTO` | Añadido campo `selectedTags` |
| `RecommenderController` | Reescrito para usar RecommendationEngine |
| `recommender.html` | UI renovada con chips de tags, score rings, badges, breakdowns |

### Principios de diseño:
- **Single Responsibility:** Cada señal es un método independiente y testeable
- **Open/Closed:** Nuevas señales se añaden sin modificar las existentes
- **Dependency Injection:** El engine recibe sus dependencias por constructor (Spring)
- **Immutability:** `UserInterestProfile` y `RecommendationWeights` son inmutables
- **Package-private visibility:** Los métodos de señales son package-private para testabilidad directa sin ser parte de la API pública

---

## 6. Tests Unitarios

**30+ tests cubriendo:**
- Jaccard Ponderado (edge cases: vacío, match perfecto, parcial, pesos)
- Wilson Score (1×5★ < 50×4★, normalización, monotonía)
- Content Match (todos satisfechos, parcial, sin filtros)
- Diversidad (sin overlap, overlap total, perfil vacío)
- Cosine Similarity (idénticos, sin comunes, similares)
- MMR (primer item, promoción de diversidad, límite, lista vacía)
- Pesos adaptativos (suma=1 para todos los contextos)
- Determinación de contexto (enum transitions)
- Perfil de usuario (construcción, filtrado de ratings <4)
- Explicabilidad (señal dominante → texto correcto)
- Pipeline completo (anónimo, login con ratings, exclusión de rated)

---

## 7. Métricas de Evaluación Sugeridas (para la memoria)

Para evaluar el sistema en el TFG se podrían usar:

| Métrica | Qué mide | Cómo calcularla |
|---------|----------|-----------------|
| Precision@K | Relevancia de las top-K recomendaciones | % de recomendaciones que el usuario valoraría ≥4 |
| NDCG | Calidad del ranking | Comparar orden producido vs. orden ideal |
| ILS (Intra-List Similarity) | Diversidad de la lista | 1 - promedio Jaccard entre todos los pares |
| Coverage | Cobertura del catálogo | % de asignaturas que aparecen en alguna recomendación |
| Cold-start performance | Calidad sin datos | Comparar scores con 0, 1, 3, 10 ratings |

---

## 8. Trabajo Futuro

- **Evaluación A/B:** Comparar recomendaciones con/sin CF, con/sin MMR
- **Implicit signals:** Tiempo en página de asignatura, clics repetidos
- **Temporal decay:** Ratings más recientes pesan más
- **Group recommendations:** Recomendar para un grupo de amigos que eligen juntos
- **Feedback loop:** El usuario puede marcar "No me interesa" para refinar el perfil

---

## 9. Stack Tecnológico

- **Backend:** Spring Boot 3 + Spring Data JPA
- **Frontend:** Thymeleaf + Tailwind CSS
- **Testing:** JUnit 5 + Mockito + AssertJ
- **BD:** H2 (desarrollo) / PostgreSQL (producción)
- **Sin dependencias externas de ML** — todo se ejecuta in-memory, síncrono, en el request HTTP
