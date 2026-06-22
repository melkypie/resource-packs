# AGENTS.md — Resource Packs Plugin

A RuneLite plugin that lets users customize the Old School RuneScape UI through resource packs — overriding widget colors, dimensions, sprites, chat colors, and game frame elements via TOML configuration files.

- **Package**: `melky.resourcepacks`
- **Entry point**: [`ResourcePacksPlugin`](src/main/java/melky/resourcepacks/ResourcePacksPlugin.java)
- **License**: BSD 2-clause (copyright header required on every source file)

## Build & Verify

```bash
gradlew compileTestJava   # PRIMARY — compile main + test sources
gradlew compileJava       # Main sources only
gradlew test              # Run tests
gradlew checkstyleMain    # Checkstyle (opt-in)
gradlew pmdMain           # PMD (opt-in)
```

## TOML Pack System

### Pack File Structure

Resource packs are loaded from the user's pack directory with these expected files:

| File | Purpose |
|------|---------|
| `vars.toml` | Variable definitions (semantic color names → hex values) |
| `overrides.toml` | Widget property overrides (color, opacity, border) |
| `chat_colors.toml` | Chat color overrides |
| `sources.toml` | Widget source definitions (bundled in plugin JAR) |

If `overrides.toml` is missing, [`PackReader`](src/main/java/melky/resourcepacks/features/packs/PackReader.java) falls back to legacy `color.properties` using [`backwards-map.properties`](src/main/resources/overrides/backwards-map.properties) to translate keys.

### Variable Resolution

`"${var_name}"` placeholders in TOML files are resolved **before** TOML parsing by [`VarResolver`](src/main/java/melky/resourcepacks/features/packs/VarResolver.java), invoked from [`PackReader.build()`](src/main/java/melky/resourcepacks/features/packs/PackReader.java).

**Three var layers, merged in order:**
1. **ID vars** — [`PackVars`](src/main/java/melky/resourcepacks/features/packs/PackVars.java) (auto-generated) maps InterfaceID names → numeric IDs (e.g., `"BANKMAIN"` → `InterfaceID.BANKMAIN`)
2. **Default vars** — bundled [`vars.toml`](src/main/resources/overrides/vars.toml) shipped with the plugin
3. **User vars** — `vars.toml` from the user's pack directory (overrides defaults)

**Two resolvers:** `PackReader` creates a **default resolver** (layers 1+2, used for `sources.toml`) and a **full resolver** (layers 1+2+3, used for `overrides.toml` and `chat_colors.toml`). This means user vars override defaults in overrides/chat_colors, but sources always use defaults.

`resolveContent()` processes raw TOML line-by-line, finding quoted `"${var}"` placeholders via regex `"\$\{([\w.]+)}"`, replacing them with raw values (quotes removed). Supports nested/recursive references, dotted keys (e.g., `"${color.border_outer}"`), and arrays (e.g., `children=["${MY_VAR}"]`).

> **IMPORTANT**: Variable resolution happens at the string level before TOML parsing. Downstream consumers receive already-resolved `Long` values, not template strings. Never add runtime variable resolution in consumers — all resolution goes through `VarResolver`.

### Pack Model

[`Pack`](src/main/java/melky/resourcepacks/model/Pack.java) is a Lombok `@Value @Builder` immutable value object:
- `vars` — `Map<Object, Object>` of user variable definitions (kept for reference, not used for runtime resolution)
- `sources` — `TomlTable` from `sources.toml` (resolved with default vars only)
- `overrides` — `TomlTable` from `overrides.toml` (variables already resolved)
- `chatColors` — `TomlTable` from `chat_colors.toml` (variables already resolved)

## Testing

### Test Pack Builder

[`TestPackBuilder`](src/test/java/melky/resourcepacks/harness/TestPackBuilder.java) constructs `Pack` objects from test TOML resources in [`src/test/resources/overrides/tests/`](src/test/resources/overrides/tests/):

```java
// Basic pack without vars
var pack = packBuilder()
    .chatColorsFromFile("chat_colors.toml")
    .build()
    .toPack();

// Pack with variable resolution — vars MUST be loaded first
var pack = packBuilder()
    .varsFromFile("vars.toml")                    // Load vars first
    .chatColorsWithTemplates("chat_colors.toml")  // Then use template-aware loader
    .build()
    .toPack();
```

### Key Test Classes

| Test | What it tests |
|------|--------------|
| [`VarResolverTest`](src/test/java/melky/resourcepacks/features/packs/VarResolverTest.java) | `VarResolver` — variable substitution, nested vars, circular refs, dotted keys, arrays |
| [`PackReaderTest`](src/test/java/melky/resourcepacks/features/packs/PackReaderTest.java) | `PackReader` — pack loading and parsing |
| [`ChatColorsTest`](src/test/java/melky/resourcepacks/features/overrides/ChatColorsTest.java) | Chat color parsing, save/apply/reset lifecycle |
| [`OverridesTest`](src/test/java/melky/resourcepacks/features/overrides/OverridesTest.java) | Widget property override parsing |
