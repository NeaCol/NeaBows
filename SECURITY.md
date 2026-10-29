# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Security Architecture

NeaBows enforces strict server-side projectile validation:
- **Persistent Data Container (PDC):** Bow identities and special attributes are stored cryptographically and validated via `NamespacedKey` PDC instances rather than easily forgeable item display names or lore.
- **Projectile Entity Validation:** Arrow spawn events, velocities, and trajectory vectors are validated server-side to prevent client-side packet spoofing or malicious velocity injection.
- **Particle & Sound Flooding Protection:** Cooldown algorithms limit particle spawning rates to prevent client-side GPU lag attacks or crash-packet exploits.
- **Kali Linux & Network Auditing:** We welcome ethical testing using Kali Linux networking toolsets (packet capture via Wireshark, automated fuzzing of command payloads).

## Reporting Vulnerabilities

If you discover an exploit, dupe vector, or security issue:
1. Do not open a public issue on GitHub.
2. Email security details or open a private advisory directly on GitHub.
3. Include reproducible steps, Paper server build, and any packet logs.
4. We aim to acknowledge reports within 48 hours.
