MVN=mvnw

.PHONY: build run test clean install fmt validate

# Ejecutar la aplicación sin empaquetar (ideal para desarrollo)
run:
	$(MVN) spring-boot:run

# Compilar y empaquetar el proyecto
build:
	$(MVN) clean package

# Ejecutar los tests
test:
	$(MVN) test

# Limpiar archivos generados
clean:
	$(MVN) clean

# Instalar artefacto en el repositorio local (usado a veces para proyectos multi-módulo)
install:
	$(MVN) clean install

# Formatear código (solo si tienes configurado el plugin de formatter)
fmt:
	$(MVN) fmt:format

# Validar el proyecto (compilar + verificar dependencias, útil en CI)
validate:
	$(MVN) validate
