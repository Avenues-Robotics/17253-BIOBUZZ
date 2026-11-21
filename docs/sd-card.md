---
title: The SD card
nav_order: 7
---

# The SD card
{: .no_toc }

1. TOC
{:toc}

The pathing tables and the drivetrain model are too big to ship inside the app, so they live on a
microSD card in the Control Hub.

## Formatting

Format the card as **FAT32**. A card the Control Hub can't mount stops the opMode at INIT with
`SD Card not mounted, did you format it to FAT32?`.

Tables for a full-resolution grid can run to several gigabytes per target, so use a large, reasonably
fast card.

## What goes on it

Everything sits at the **root** of the card:

```
/MANIFEST.JSON          what's on the card: grid, encoding, list of targets
/MODEL.JSON             the fitted drivetrain model and the honing PID gains
/TABLES/T00C0000.BIN    target 00, chunk 0000
/TABLES/T00C0001.BIN    target 00, chunk 0001
/TABLES/T01C0000.BIN    target 01, chunk 0000
...
```

You never write these by hand. The desktop solver (`peregrine-desktop`) generates all of them.
Copy them onto the card exactly as it outputs them.

The robot also creates this folder on the card, for [calibration](calibration.md) and other
`Logger` logs:

```
/Android/data/com.qualcomm.ftcrobotcontroller/files/logs/
```

## Checking a card

Before a match, run the desktop project's verifier against the card:

```bash
py -3.12 wizard/verify_tables.py G:\
```

(Replace `G:\` with wherever the card is mounted.) It checks file counts and sizes, checksums, that
the model is valid, and that the values make physical sense.

## What the robot does with it

- At INIT, the robot reads `MANIFEST.JSON` and `MODEL.JSON` and opens every table file. opModes that
  [opt out](opmodes.md#opting-out-of-the-sd-card-tables) skip all of this and never touch the tables.
- It checks that the tables are in the field frame and use a supported storage type (`u8`, `u16`,
  `f16` or `f32`). `u16` is the default and the recommended choice.
- While driving, it reads 7 values per loop from the files that are already open.
- When the opMode ends, it closes the files.

If anything is missing or invalid, the opMode stops at INIT and the Driver Station shows an error
message saying what's wrong. See [Troubleshooting](troubleshooting.md).

## Swapping cards

Power the Control Hub off before removing or inserting a card. The new tables are picked up the next
time an opMode is initialised.

## The full format

The byte-level format (manifest fields, indexing, encoding, the drivetrain model and the honing gains)
is specified in `TABLE_FORMAT.MD`, which ships with the desktop solver. You only need it if you are
changing the solver or the robot's table reader.
