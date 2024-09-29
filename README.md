# vd-tool: Vector Drawable Command-Line Tool

This repository contains vd-tool - command-line tool for converting SVG
files to Android Vector Drawable XML files.
It's based on the [vector drawable tool](https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-main/vector-drawable-tool/)
from the Android Studio source code, repackaged for standalone use.

## Building

The project uses Gradle for building. The main artifact is a self-contained, minimized JAR file created using the Shadow plugin and ProGuard.

To build the project:

```bash
./gradlew proguardJar
```

This will create a minimized JAR file at `build/libs/vd-tool-min.jar`.

## Usage

After building, you can run the tool using:

```bash
java -jar build/libs/vd-tool-min.jar [options]
```

### Command-line Options

```
Usage: -c -in <file or directory> -out <directory> [-widthDp <size>] [-heightDp <size>] [-addHeader]
Options:
  -c                        Convert SVG files to VectorDrawable XML.
  -in <file or directory>   Input SVG file or directory containing SVG files.
  -out <directory>          Output directory for converted XML files.
  -widthDp <size>           Force the width to be <size> dp, <size> must be integer.
  -heightDp <size>          Force the height to be <size> dp, <size> must be integer.
  -addHeader                Add AOSP header to the top of the generated XML file.
```

### Example

To convert all SVG files in a directory:

```bash
java -jar build/libs/vd-tool-min.jar -c -in path/to/svg/files -out path/to/output/directory
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.