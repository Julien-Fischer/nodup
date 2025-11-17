#!/usr/bin/env bash

install_maven_if_not_present() {
  if ! command -v mvn >/dev/null 2>&1; then
    read -r -p "Maven is required to compile nodup the first time. Do you want to install it now? (Y/n):" user_input
    user_input=${user_input:-Y}
    if [[ "${user_input,,}" =~ ^(y|yes)$ ]]; then
      sudo apt update && sudo apt install -y maven
      echo "Maven installation completed."
    else
      echo "Maven installation aborted by user."
      exit 1
    fi
  fi
  mvn --version
}

# shellcheck disable=SC2120
install() {
  local jar_path="${HOME}/nodup"
  local jar_name="nodup-1.0-SNAPSHOT.jar"
  local repository="https://github.com/Julien-Fischer/nodup.git"
  local is_path_provided=
  is_path_provided=false

  if [[ -n "${1}" ]]; then
    is_path_provided=true
  else
    echo "Installing nodup..."
  fi

  if ! sudo -v; then
    echo "This script requires sudo privileges. Exiting."
    exit 1
  fi

  install_maven_if_not_present


script_content=$(cat <<EOF
#!/usr/bin/env bash

update_nodup() {
  local tmpdir
  tmpdir=\$(mktemp -d)

  echo "Updating nodup..."
  echo "Downloading latest version..."

  if git clone --quiet --depth 1 --branch main "${repository}" "\${tmpdir}" >/dev/null; then
      echo "> OK"
  else
      echo "Failed to download latest version"
  fi

  cd "\${tmpdir}" || { echo "cd failed!" >&2; exit 1; }

  chmod +x ./src/main/resources/install.sh && ./src/main/resources/install.sh "\${tmpdir}"

  cd - || exit 1
  rm -rf "\${tmpdir}"
}

if [[ "\${1}" = "update" ]]; then
  update_nodup
else
  java -jar ${jar_path}/${jar_name} \$@
fi
EOF
)

  is_path_provided
  if $is_path_provided; then
      cd "${1}" || exit
  fi

  echo "Compiling nodup..."

  if mvn clean install > build.log 2>&1 &&  mvn -s ./maven-config.xml clean install > build.log 2>&1; then
      echo "> OK"
  else
    echo "Maven clean install failed"
    cat build.log
  fi

  mkdir -p "${jar_path}"
  cp "./target/${jar_name}" "${jar_path}"
  echo "${script_content}" > nodup && chmod +x nodup && sudo mv nodup /usr/local/bin
}

install
