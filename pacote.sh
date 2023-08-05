#!/bin/bash

# Define a string de origem e a string de destino
ORIGEM="com.arena.esportes"
DESTINO="com.arena.esportes"

# Localiza todos os arquivos no diretório atual e subdiretórios
for file in $(find . -type f)
do
  # Verifica se a string de origem está no arquivo
  if grep -q $ORIGEM $file
  then
    # Substitui a string de origem pela string de destino
    sed -i "s/$ORIGEM/$DESTINO/g" $file
    echo "Arquivo $file modificado."
  fi
done
