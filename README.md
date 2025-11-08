# Mecon for MOFUCRAFT!!! 
Economy base plugin for Bukkit/Spigot  
[日本語解説](https://e-craft.io/bukkit/plugin/mecon/)

## Mecon features
* UUID Ready
* 1.15 Ready
* Vault Ready
* MySQL(+SQLite) Ready
* Command tab complete
* Easy to use
* API available

## For MOFUCRAFT!!! features
* Added a table prefix
* Added in-game colors
* Disabled a decimal currency
* Changed in-game prefix


## Command/Permission
|Command|Permission|Description|Default|
|:------|:---------|:----------|:------|
|/money|mecon.show|Show your balance.|ALL|
|/money show [player]|mecon.show.other|Show [player] balance.|OP|
|/money pay &lt;player&gt; &lt;amount&gt;|mecon.pay|Send &lt;amount&gt; to &lt;player&gt;.|ALL|
|/money set &lt;player&gt; &lt;balance&gt;|mecon.set|Set the balance of &lt;player&gt; to &lt;balance&gt;.|OP|
|/money give &lt;player&gt; &lt;amount&gt;|mecon.give|Give &lt;amount&gt; to &lt;player&gt;.|OP|
|/money take &lt;player&gt; &lt;amount&gt;|mecon.take|Take &lt;amount&gt; on &lt;player&gt;.|OP|
|/money create &lt;player&gt; [balance]|mecon.create|Create &lt;player&gt; account.|OP|
|/money remove &lt;player&gt;|mecon.remove|Remove &lt;player&gt; account.|OP|
|/money top [page]|mecon.top|Show billionaires ranking.|OP|
|/money convert|mecon.convert|Convert database.|OP|
|/money reload|mecon.reload|Reload the config.|OP|
|/money version|mecon.version|Show version and check new version.|OP|
|/money help|N/A|Show helps.|ALL|

# API
## Maven
```xml
<project>
    <repositories>
        <repository>
            <id>himajyun-repo</id>
            <url>https://himajyun.github.io/mvn-repo/</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>jp.jyn</groupId>
            <artifactId>Mecon</artifactId>
            <version>2.2.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

## Usage
```java
public class Main extends JavaPlugin {
    private Mecon mecon;

    @Override
    public void onEnable() {
        // get plugin
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Mecon");
        if(plugin == null || !plugin.isEnabled()) {
            // not available
            getLogger().warning("Mecon is not available.");
        }

        this.mecon = (Mecon) plugin;
    }

    public void usage(UUID uuid) {
        // get
        mecon.getRepository().getDecimal(uuid);

        // set
        mecon.getRepository().set(uuid, BigDecimal.ZERO);
    }
}
```
