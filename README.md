# NeaBows

<p align="center">
  <img src="https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Paper-1.21.1-1F232A?style=for-the-badge&logo=paper&logoColor=white" alt="Paper 1.21.1" />
  <img src="https://img.shields.io/badge/Storage-PersistentDataContainer-005571?style=for-the-badge" alt="PDC Storage" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License MIT" />
</p>

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,maven,git,linux" alt="Tech Stack" />
  </a>
</p>

<p align="center">
  <b>High-performance elemental projectile engine and custom weaponry framework for Paper/Purpur 1.21+.</b><br>
  <i>8 elemental disciplines • 3-tiered progression mechanics • Dynamic particle ray-tracing • PDC metadata serialization</i>
</p>

---

## Architectural Highlights

NeaBows is a specialized combat weaponry engine developed for Minecraft 1.21+ Paper and Purpur instances. Rather than utilizing brittle legacy NBT strings or display-name parsing, NeaBows employs Bukkit's native `PersistentDataContainer` (PDC) API with dedicated `NamespacedKey` contracts. This ensures item identity persistence across inventory moves, server restarts, container transactions, and cross-server transfers.

### Core Systems
- **Tick-Accurate Projectile Tracing:** Projectile trajectory particles are spawned via scheduled asynchronous/tick tasks bounded by configurable interval limits (`trail-interval`), preventing packet spam while rendering fluid motion vectors.
- **PDC Metadata Integrity:** Weapon powers, levels, and unique identifiers are serialized as binary PDC primitives, preventing spoofing via anvil renaming.
- **Rate-Limiting & Spam Prevention:** Built-in per-power and global cooldown state machines eliminate projectile spam attacks and maintain a constant 20.0 TPS baseline.
- **Dynamic Physics & Potion Vectors:** Custom velocity impulses (`velocidad-y`), screen-freeze timers (`freeze-ticks`), and multi-effect potion matrices applied cleanly upon entity impact.

---

## Elemental Disciplines & Mechanics

Each elemental weapon features **3 tiered progression stages**:

| Discipline | Identifier | Particle Palette | Stage 1 Effect | Stage 2 Effect | Stage 3 (Ascendant) Effect |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Fire** | `fuego` | `#FF6B35` / `#FF0000` | 3s Ignite | 6s Extended Combustion | 10s Inferno Combustion |
| **Ice** | `hielo` | `#7DD8E8` / `#FFFFFF` | Slowness I (4s) | Slowness II + 100t Freeze Screen | Slowness III + 200t Freeze + Blindness |
| **Undead** | `muertos` | `#8B8B8B` / `#333333` | Wither I (5s) | Wither II (7s) | Wither III (9s) + Darkness |
| **Wind** | `viento` | `#C0E8FF` / `#87CEEB` | Upward Impulse (Y: 1.2) | Upward Impulse (Y: 1.8) + Levitation | Skyward Vortex (Y: 2.5) + Levitation II |
| **Scorpion** | `escorpion` | `#32CD32` / `#006400` | Poison I (5s) | Poison II (7s) | Lethal Poison III (9s) + Weakness |
| **Amethyst** | `amatista` | `#9966CC` / `#4B0082` | Triple Projectile Spread | Triple Spread + Weakness I | Triple Spread + Weakness II + Blindness |
| **Bone** | `hueso` | `#F5F5DC` / `#D2B48C` | Mining Fatigue I (5s) | Mining Fatigue II + Slowness I | Max Mining Fatigue III + Slowness II |
| **Earth** | `tierra` | `#A0522D` / `#8B4513` | Blindness (3s) | Blindness + Slowness | Crushing Earth: Blindness + Slowness + Nausea |

---

## Command Reference & Permissions

Primary command: `/neabow` (Alias: `/nb`)

| Command | Arguments | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/neabow give` | `<player> <power> <tier>` | `neabows.give` | Spawns and awards an elemental bow to the target player |
| `/neabow set` | `<power> <tier>` | `neabows.use` | Enchants the held bow with the specified elemental power |
| `/neabow remove` | _None_ | `neabows.use` | Strips all elemental PDC metadata from the held bow |
| `/neabow list` | _None_ | `neabows.use` | Displays available elemental disciplines and status |
| `/neabow reload` | _None_ | `neabows.admin` | Hot-reloads `config.yml` and `poderes.yml` definitions |

---

## Configuration

### Core Settings (`config.yml`)
```yaml
# ==============================================================================
# NeaBows - Core Engine Configuration
# ==============================================================================

settings:
  trail-particles: true
  trail-interval: 2      # Particle frequency in ticks (2 = dense, 4 = balanced)
  affect-players: true   # Whether elemental abilities affect other players
  affect-self: false     # Whether self-inflicted hits apply elemental effects

cooldowns:
  global: 3              # Global fallback cooldown in seconds
  per-power:
    fuego: 3
    hielo: 4
    muertos: 5
    viento: 3
    amatista: 5
```

---

## Security & Anti-Exploit Principles

- **PDC Cryptographic Isolation:** Client-sent NBT modifications or item display alterations cannot inject elemental powers; powers are evaluated strictly via server-side `PersistentDataContainer` matching `me.neabows` keys.
- **Rate-Limiting (TPS Protection):** Prevents auto-clicker bow spam from overloading the entity tracker or particle broadcasting queues.
- **Trajectory Bounding:** Projectile vectors and velocity calculations are validated to prevent client packet injection of abnormal velocities.

---

## Building from Source

Requirements:
- **Java 21 (JDK)**
- **Apache Maven 3.8+**

```bash
# Clone the repository
git clone https://github.com/NeaCol/NeaBows.git
cd NeaBows

# Build and package
mvn clean package
```

The compiled JAR will be output to `target/NeaBows-1.0.0.jar`.

---

## Spanish Documentation / Documentación en Español

<details>
<summary><b>Haz clic aquí para ver la documentación en Español</b></summary>

### Descripción General
**NeaBows** es un plugin de armamento mágico y combate táctico para servidores Minecraft Paper/Purpur 1.21+. Permite forjar arcos imbuidos con 8 disciplinas elementales, rastros de partículas de alta precisión y mecánicas de impacto destructivas.

### Características Principales
- **Persistencia Nativa PDC:** Almacenamiento directo en el PersistentDataContainer del ítem; inmune a alteraciones de nombre o transferencias entre inventarios.
- **8 Disciplinas con 3 Niveles:** Progresión de habilidades (Fuego, Hielo, Muertos, Viento, Escorpión, Amatista, Hueso y Tierra).
- **Control de Cooldowns:** Temporizadores independientes por elemento para salvaguardar el TPS del servidor contra exploits de spam.
- **Rastreo de Flechas:** Estelas de partículas optimizadas con intervalos configurables para no saturar la conexión del cliente.

</details>

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
