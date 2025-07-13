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

```
Usage:
  nodup /path/to/dir [OPTIONS]

Options:
  --log            Set the logging level (e.g., severe, warning, info, fine, finer, finest).

Flags:
  -c, --copy       Copy files in the directory.
  -m, --move       Move files in the directory.
  -s, --scan       Scan the directory and display file information.
```


## Requirements

  - Java 21
