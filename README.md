# ThinGL
Lightweight Java wrapper for many common OpenGL functions.

## Features
- Easy to integrate in any LWJGL3 (>= 3.3.3) project
  - ThinGL reverts all OpenGL states to the previous state after rendering
  - Confirmed to work in Minecraft and Cosmic Reach
  - Supports GLFW and SDL3
- Uses modern OpenGL (OpenGL 4.5)
- Builtin shaders
  - Simple color and texture shaders
  - Variable width lines
  - Post-processing, such as blur or object outlining
- Text rendering
  - TrueType font support
  - Bitmap, SDF and BSDF text rendering
  - Optional HarfBuzz integration for complex text
- Easy to use rendering abstraction
  - Draw call batching
  - Immediate mode rendering
  - Retained mode rendering
  - Basic Instancing support
  - Basic Multidraw support
- Wrapper classes for OpenGL objects and functions
- Pretty fast (Built with performance in mind without sacrificing usability and readability)
- And much more...

## Releases
### Gradle/Maven
To use ThinGL with Gradle/Maven you can get it from [Lenni0451's Maven](https://maven.lenni0451.net/#/snapshots/net/raphimc/thingl) or [Jitpack](https://jitpack.io/#RaphiMC/ThinGL).
You can also find instructions how to implement it into your build script there.

### Jar File
If you just want the latest jar file you can download it from [GitHub Actions](https://github.com/RaphiMC/ThinGL/actions/workflows/build.yml) or [Lenni0451's Jenkins](https://build.lenni0451.net/job/ThinGL/).

## Usage and Examples
Examples can be found in the [src/example](/src/example) directory.

### Logging
ThinGL by default logs to System.out and System.err.
You can however easily change this by calling ``LoggerFactory.setBuilder(Slf4jLogger::new);`` to for example log through SLF4J.

## Contact
If you encounter any issues, please report them on the
[issue tracker](https://github.com/RaphiMC/ThinGL/issues).  
If you just want to talk or need help implementing ThinGL feel free to join my
[Discord](https://discord.gg/dCzT9XHEWu).
