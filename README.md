# Dilly Awards Arena

A Paper plugin that builds a large navy, electric-blue and lime-green Dilly Awards ceremony arena around the command user's current position, facing direction.

## Requirements
- Paper server (built against Paper API 1.21.4; intended for modern Paper servers)
- Java 21 or newer
- Operator or `dillyawards.admin` permission

## Build
Upload this project to a GitHub repository. The included GitHub Actions workflow builds the plugin. Download `DillyAwardsArena.jar` from the workflow's Artifacts section.

## Install
1. Put `DillyAwardsArena.jar` in the server's `plugins` folder.
2. Restart the server.
3. Stand at the front edge of a clear, reasonably flat area, facing toward where you want the stage.
4. Run `/awards build`.

The arena extends forward from you. It includes a large platform, central red-carpet aisle, audience seating, stage, host podium, large display wall, green D logo, lighting towers and backstage space.

## Commands
- `/awards build` — build the arena in front of you.
- `/awards clear` — restore the exact blocks that were in the arena footprint before the last build.
- `/awards help` — show commands.

## Important
Build on a clear area. The plugin records original block data and restores it with `/awards clear`. Do not manually edit or remove the arena snapshot file while an arena is built. Building is refused if a previous arena snapshot still exists; clear the old arena first.
