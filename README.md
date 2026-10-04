# 🏹 NeaBows

<p align="center">
  <b>Custom Elemental Bows & Special Weapons Engine for Minecraft</b><br>
  <i>8 Elemental Powers • 3 Evolutionary Levels • Dynamic RGB Particles • Built-in Cooldown Engine</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21%2B-brightgreen.svg" alt="Minecraft Version" />
  <img src="https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot%20%7C%20Purpur-orange.svg" alt="Platform" />
  <img src="https://img.shields.io/badge/Java-21-blue.svg" alt="Java 21" />
  <img src="https://img.shields.io/badge/Mechanics-Persistent%20Data%20Containers-red.svg" alt="PDC" />
</p>

---

## 📌 Descripción General

**NeaBows** es un plugin de armamento mágico y combate RPG para Minecraft 1.21+. Permite crear arcos imbuidos con poderes elementales únicos, rastros de partículas dinámicas y efectos de impacto devastadores.

Utilizando los contenedores de datos persistentes nativos de Bukkit (**PDC**), los poderes y niveles se guardan de forma permanente en los metadatos del objeto sin depender de NBTs frágiles, haciéndolo compatible con cualquier sistema de inventarios, cajas de recompensas o tiendas.

---

## 🔮 Los 8 Poderes Elementales

Cada elemento cuenta con **3 niveles de poder** progresivos y configurables:

| Elemento | Icono | Descripción del Efecto en Impacto |
| :--- | :---: | :--- |
| **Fuego** | 🔥 | Enciende en llamas al objetivo de 3 a 10 segundos continuos. |
| **Hielo** | ❄️ | Aplica Lentitud, efecto visual de congelamiento de pantalla (`freeze-ticks`) y ceguera. |
| **Muertos** | 💀 | Descompone al objetivo con efecto Wither y Oscuridad profunda (*Darkness*). |
| **Viento** | 🌪️ | Lanza al objetivo violentamente por los aires con levitación forzada. |
| **Escorpión** | 🦂 | Inocula Veneno letal prolongado combinado con Debilidad. |
| **Amatista** | 💎 | **Mecánica especial:** Disparo triple simultáneo de flechas + Debilidad y Ceguera. |
| **Hueso** | 🦴 | Provoca Fatiga de Minería severa y Lentitud corporal extrema. |
| **Tierra** | 🪨 | Aplica Ceguera pesada, Lentitud de movimiento y Náuseas desorientadoras. |

---

## ✨ Características del Motor de Combate

- **✨ Rastros de Partículas en Vuelo:** Las flechas generan estelas de partículas dinámicas con densidad e intervalos configurables.
- **🔊 Efectos de Sonido Inmersivos:** Sonidos dedicados al tensar, disparar e impactar mediante la API de sonidos de Minecraft 1.21.
- **⏱️ Motor de Cooldowns Inteligente:** Sistema de enfriamiento global o por poder individual para evitar el spam en zonas PvP competitivas.
- **🛡️ Filtros de Daño y Fuego Amigo:** Configuración para decidir si los poderes afectan a otros jugadores y/o al propio tirador.

---

## 📋 Comandos y Permisos

### Comandos
| Comando | Descripción | Permiso |
| :--- | :--- | :--- |
| `/neabow give <jugador> <poder> <nivel>` | Entrega un arco con el poder y nivel seleccionado | `neabows.give` |
| `/neabow set <poder> <nivel>` | Asigna un poder al arco que sostienes en la mano | `neabows.use` |
| `/neabow remove` | Elimina el poder del arco sostenido | `neabows.use` |
| `/neabow list` | Lista todos los poderes disponibles | `neabows.use` |
| `/neabow reload` | Recarga los archivos `config.yml` y `poderes.yml` | `neabows.admin` |

*Alias disponibles: `/nb`*

---

## ⚙️ Configuración (`poderes.yml`)

Los poderes son totalmente personalizables con duraciones en ticks (20 ticks = 1s):
```yaml
poderes:
  fuego:
    nombre: "Fuego"
    habilitado: true
    color: "#FF6B35"
    max-nivel: 3
    niveles:
      1:
        descripcion: "Prende fuego al objetivo por 3 segundos"
        duracion-fuego: 60
      2:
        descripcion: "Prende fuego al objetivo por 6 segundos"
        duracion-fuego: 120
      3:
        descripcion: "Infierno: fuego devastador por 10 segundos"
        duracion-fuego: 200
```

---

## 🛠️ Instalación y Compilación

1. **Instalación:**
   - Coloca `NeaBows.jar` en la carpeta `/plugins/` de tu servidor Paper o Spigot 1.21+.
   - Reinicia el servidor para generar los archivos de configuración.

2. **Compilación:**
```bash
git clone https://github.com/NeaCol/NeaBows.git
cd NeaBows
mvn clean package
```

---

<p align="center">
  Desarrollado con dedicación por <b>NeaBows</b> • Innovación para Combate en Minecraft
</p>
