#!/usr/bin/env bash
set -e

# Synchronization of official photo of Alzira Joné into Android resources
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET="$ROOT_DIR/app/src/main/res/drawable/official_app_identity_image.png"
RES_BASE="$ROOT_DIR/app/src/main/res"

echo "=== Sincronizador de Identidade Visual Oficial: ALZIRA JONÉ ==="

# Search for the uploaded official photo in the project (placed via File Explorer)
FOUND_SOURCE=$(find "$ROOT_DIR" -maxdepth 5 \
  -not -path "*/.gradle/*" \
  -not -path "*/build/*" \
  -not -path "*/.build-outputs/*" \
  -not -name "official_app_identity_image.png" \
  -not -name "ic_launcher*" \
  \( -iname "*capa*.jpeg" -o -iname "*capa*.jpg" -o -iname "*capa*.png" -o -iname "*alzira*.jpeg" -o -iname "*alzira*.jpg" -o -iname "*alzira*.png" -o -iname "*foto*.jpeg" -o -iname "*foto*.jpg" -o -iname "*foto*.png" -o -iname "*photo*.jpeg" -o -iname "*photo*.jpg" -o -iname "*photo*.png" \) \
  2>/dev/null | head -n 1)

if [ -z "$FOUND_SOURCE" ]; then
  if [ -f "$TARGET" ]; then
    FOUND_SOURCE="$TARGET"
  fi
fi

if [ -n "$FOUND_SOURCE" ] && [ -f "$FOUND_SOURCE" ]; then
  echo "Foto oficial detectada em: $FOUND_SOURCE"
  
  # Validar que a foto pode ser lida
  identify "$FOUND_SOURCE" > /dev/null 2>&1 || {
    echo "ERROR: Arquivo de imagem inválido ou corrompido em $FOUND_SOURCE"
    exit 1
  }

  # Preservação absoluta da foto original: redimensionamento técnico direto sem crop artificial
  if [ "$FOUND_SOURCE" != "$TARGET" ]; then
    convert "$FOUND_SOURCE" -resize "1024x1024!" "$TARGET"
    echo "Recurso oficial canonical atualizado: $TARGET"
  fi

  # Gerar mipmaps diretamente da fotografia original com preservação total de enquadramento
  for spec in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
    density="${spec%:*}"
    size="${spec#*:}"
    dir="$RES_BASE/mipmap-$density"
    mkdir -p "$dir"
    rm -f "$dir/ic_launcher.webp" "$dir/ic_launcher_round.webp"
    # Ícone launcher padrão (PNG32 quadrado com foto real)
    convert "$TARGET" -resize "${size}x${size}!" "PNG32:$dir/ic_launcher.png"
    # Ícone launcher redondo (máscara circular técnica padrão Android com margem de segurança para o cabelo)
    radius=$((size / 2))
    inner_size=$((size * 88 / 100))
    convert "$TARGET" -resize "${inner_size}x${inner_size}" \
      -gravity center -background "#675B4B" -extent "${size}x${size}" \
      \( -size "${size}x${size}" xc:none -fill white -draw "circle $radius,$radius $radius,0" \) \
      -alpha set -compose DstIn -composite "PNG32:$dir/ic_launcher_round.png"
  done
  echo "Todos os ícones (launcher normal e redondo) foram atualizados com sucesso em todos os mipmaps com a fotografia real!"
  exit 0
else
  echo "ERROR: official Alzira photo was not found in project."
  echo "Upload the original photo through the File Explorer (e.g., Capa.jpeg in project root or app/src/main/res/drawable/)."
  echo "Build stopped intentionally."
  exit 1
fi


