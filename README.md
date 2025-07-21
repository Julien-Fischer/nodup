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

  2. Optionally, check `~/nodup/bin` for the scan / removal results


## API

```
Usage:
  nodup [/path/to/dir] [OPTIONS]

Positional parameters:
  $1               (Optional) The path to the directory to process  
  
Subcommands:
  update           Update to the latest version
  bin
      --list       List all bin directories
      --path       Print bin path
      --clear      Delete all bin directories
      --open       Open the bin directory (requires a GUI environment)

Options:
  --log            Set the logging level (e.g., severe, warning, info, fine, finer, finest).

Flags:
  -c, --copy       Copy files in the directory.
  -m, --move       Move files in the directory.
  -s, --scan       Scan the directory and display file information.
  -h, --help       Print this help message and exit
```


## Requirements

  All platforms:

  - Java 21

  For Linux, the current installation script requires:

  - Bash
  - Maven
