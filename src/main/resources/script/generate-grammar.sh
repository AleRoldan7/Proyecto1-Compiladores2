#!/bin/bash

# ==========================================
# CONFIGURACIÓN DEL PROYECTO
# ==========================================

ANTLR_DIR="src/main/antlr4/org/compi2/proyecto1compiladores2"
OUTPUT_DIR="src/main/generated"

# ==========================================
# LIMPIAR GENERACIÓN ANTERIOR
# ==========================================

echo "=========================================="
echo " Limpiando archivos generados..."
echo "=========================================="

rm -rf "$OUTPUT_DIR"

mkdir -p "$OUTPUT_DIR"

# ==========================================
# GENERAR GRAMMAR PIGLATIN
# ==========================================

echo ""
echo "Generando GrammarPigLatin.g4..."

mkdir -p "$OUTPUT_DIR/piglatin"

antlr4 \
    -visitor \
    -listener \
    -package org.compi2.proyecto1compiladores2.piglatin \
    -o "$OUTPUT_DIR/piglatin" \
    "$ANTLR_DIR/GrammarPigLatin.g4"

# ==========================================
# GENERAR GRAMMAR PYTHON
# ==========================================

echo ""
echo "Generando GrammarPython.g4..."

mkdir -p "$OUTPUT_DIR/python"

antlr4 \
    -visitor \
    -listener \
    -package org.compi2.proyecto1compiladores2.python \
    -o "$OUTPUT_DIR/python" \
    "$ANTLR_DIR/GrammarPython.g4"

# ==========================================
# GENERAR GRAMMAR ZETARIANO
# ==========================================

echo ""
echo "Generando GrammarZetariano.g4..."

mkdir -p "$OUTPUT_DIR/zetariano"

antlr4 \
    -visitor \
    -listener \
    -package org.compi2.proyecto1compiladores2.zetariano \
    -o "$OUTPUT_DIR/zetariano" \
    "$ANTLR_DIR/GrammarZetariano.g4"

# ==========================================
# RESULTADO
# ==========================================

echo ""
echo "=========================================="
echo " ANTLR4 GENERADO CORRECTAMENTE"
echo "=========================================="
echo ""
echo "Archivos generados en:"
echo "$OUTPUT_DIR"
echo ""