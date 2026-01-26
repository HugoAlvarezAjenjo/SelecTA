ifeq ($(OS),Windows_NT)
    MVN = mvnw.cmd
    OPEN = cmd /c start
else
    UNAME_S := $(shell uname -s)
    ifneq (,$(findstring MINGW,$(UNAME_S)))
        MVN = mvnw
        OPEN = cmd /c start
    else
        MVN = ./mvnw
        ifeq ($(UNAME_S),Darwin)
            OPEN = open
        else
            OPEN = xdg-open
        endif
    endif
endif

.PHONY: build run test clean install fmt validate report

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

# Abrir el informe de cobertura de JaCoCo en el navegador
report:
	$(OPEN) target/site/jacoco/index.html
