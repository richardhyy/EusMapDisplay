# EusMapDisplay
Map-based display manager Spigot plugin with event hooking support.




## Features
* MapDisplay with cursor events support
* Support large displays by putting parts of maps together




## Demos
### Events
![Events](images/demo_test.gif)

### Application: Screen mirroring
![Application](images/demo_desktop.gif)




## Commands

| Command                                                 | Description                                                  | Permission         |
| ------------------------------------------------------- | ------------------------------------------------------------ | ------------------ |
| /mapdisplay list                                        | List existing MapDisplays                                    | `mapdisplay.admin` |
| /mapdisplay get \<UUID\> [Player] [Column(x)] [Line(y)] | Get a copy of MapDisplay (using `*` for `all columns/lines`) | `mapdisplay.admin` |
| /mapdisplay test                                        | Get MapDisplay for testing                                   | `mapdisplay.admin` |

