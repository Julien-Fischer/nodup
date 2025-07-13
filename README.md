# Image Deduplicator

Cross-platform image deduplicator with an emphasis on performance


## Getting started

### Install (Linux)
 
  1. Clone this project

     `git clone https://github.com/Julien-Fischer/nodup`

  2. Run the installer

     `./nodup/src/main/resources/install.sh`

  3. Reload your terminal

     `source ~/.bashrc`

### How to use

  1. Execute `nodup`

     `nodup ~/Images --move`

  2. Optionally, check `~/collision_bin` for the scan / removal results


## API

- `nodup /path/to/dir [-c, --copy]`
- `nodup /path/to/dir [-m, --move]`
- `nodup /path/to/dir [-s, --scan] (default)`


## Requirements

  - Java 21
