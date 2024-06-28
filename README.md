# mapid-syncer

Sync your map ids between servers!

Requires at least Paper 1.20.6.

## Configuration

Edit the mysql section in the config.yml file to match your database settings.

```yaml
mysql:
  host: "localhost"
  port: "3306"
  database: "minecraft"
  username: "root"
  password: ""
```

## Building

1. Enter the invisible-item-frames directory and run:  
   `mvn`

2. The plugin jar is available at:  
   `target/mapid-syncer-1.0.0.jar`
