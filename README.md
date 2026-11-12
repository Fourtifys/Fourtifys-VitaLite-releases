# Fourtifys VitaLite plugins

Source for the Fourtifys plugins for the VitaLite client.

| Plugin | Folder |
| --- | --- |
| Fourtifys Gem Mining | [`FourtifysGemMining`](FourtifysGemMining) |
| Fourtifys Tormented Demons | [`FourtifysTormentedDemons`](FourtifysTormentedDemons) |

Each folder is its own Gradle project. Build one with `bash gradlew build` inside its folder.
It compiles against `com.tonic:api` and `com.tonic:base-api` from Maven Local and
`net.runelite:client` from the RuneLite repository, like other VitaLite plugins.
