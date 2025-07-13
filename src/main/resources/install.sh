#!/usr/bin/env bash

echo "Installing nodup..."

if ! sudo -v; then
  echo "This script requires sudo privileges. Exiting."
  exit 1
fi


install() {
  local jar_path="${HOME}/nodup"
  local jar_name="image_processor-1.0-SNAPSHOT.jar"

local script_content
script_content=$(cat <<EOF
#!/usr/bin/env bash
java -jar ${jar_path}/${jar_name} \$@
EOF
)

  mvn -s ./maven-config.xml clean install
  mkdir -p "${jar_path}"
  cp "./target/${jar_name}" "${jar_path}"
  touch nodup && echo "${script_content}" > nodup && chmod +x nodup && sudo mv nodup /usr/local/bin
}

install
