[![Download](https://api.bintray.com/packages/nahuld/minevictus/ChatMenuAPI/images/download.svg)](https://bintray.com/nahuld/minevictus/ChatMenuAPI/_latestVersion)

# ChatMenuAPI
An API for making menus inside Minecraft's chat.
This API treats Minecraft's chat like a 2D grid, allowing you to position elements freely in chat.

### Fork

This is a fork of VGL for Minevictus where we updated the API to use Gradle, with the latest version of PaperMC. As well as setting up releases and publishing it to maven repositories for ease of access.

## Preview
![](https://sparse.blue/files/k0ejrc.gif)

---

## Contents
* [ChatMenuAPI](#chatmenuapi)
  - [Preview](#preview)
  - [Contents](#contents)
  - [Usage](#usage)
    + [Setup](#setup)
    + [ChatMenu](#chatmenu)
    + [Element](#element)
    + [States](#states)
    + [Displaying](#displaying)
  - [Links](#links)

---

## Usage

### Setup

#### Requirements
- JDK 8 or above.

#### Download:
Download `ChatMenuAPI.jar` from the releases page, then add it as a dependency in your local workspace.

#### Gradle or Maven
> Replace `VERSION` with a specific version. The latest version which can be found at
> the "Download" button or the Bintray page.

###### Maven
```xml
<repositories>
  <repository>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <name>bintray</name>
    <url>https://dl.bintray.com/nahuld/minevictus/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>me.tom.sparse</groupId>
    <artifactId>ChatMenuAPI</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```
###### Gradle
```groovy
repositories {
    maven {
        url  'https://dl.bintray.com/nahuld/minevictus' 
    }
}

dependencies {
     implementation 'me.tom.sparse:ChatMenuAPI:VERSION'
}
```
###### Gradle KTS
```kotlin
repositories {
    maven {
        url = uri("https://dl.bintray.com/nahuld/minevictus") 
    }
}

dependencies {
     implementation("me.tom.sparse:ChatMenuAPI:VERSION")
}
```
**IMPORTANT!** If you are using this plugin bundled within others, you will need to initialize and disable the API manually, which you can do by adding the following methods to your JavaPlugin class.
```java
@Override
public void onEnable() {
    ChatMenuAPI.init(this);
}    

@Override
public void onDisable() {
    ChatMenuAPI.disable();
}
```

### ChatMenu
To create a menu, just create a new instance of `ChatMenu`:
```Java
ChatMenu menu = new ChatMenu();
```
If you are not using this API just for chat formatting, it is recommended that you make the menu a pausing menu:
```Java
ChatMenu menu = new ChatMenu().pauseChat();
```
When this menu is sent to a player, it will automatically pause outgoing chat to that player so that the menu will not be interrupted.

**Warning:** If you make a menu pause chat, you need to add a way to close the menu!

### Element
Elements are the building blocks of menus. They are used to represent everything in a menu.
There are a few elements provided by default, you can view them by [clicking here](../master/src/me/tom/sparse/spigot/chat/menu/element).

Basic `TextElement`:
```Java
menu.add(new TextElement("Hello, world!", 10, 10));
```

Basic close button:
```Java
menu.add(new ButtonElement(x, y, ChatColor.RED+"[Close]", (p) -> {menu.close(p); return false;}));
```

Instead of manually creating a close button, you can also just pass the arguments you would use for a close button directly into the `pauseChat` method.
```Java
ChatMenu menu = new ChatMenu().pauseChat(x, y, ChatColor.RED+"[Close]");
```

All of the default elements require and X and Y in their constructor, 
these coordinates should be greater than or equal to 0 and less than 320 on the X axis and 20 on the Y axis.
The default Minecraft chat is 320 pixels wide and 20 lines tall.

### States
Most interactive elements have one or more `State` objects.

`State`s are used to store information about an `Element`, such as the current number in an `IncrementalElement`.

Every state can have a change callback to detect when it changes:
```Java
IncrementalElement incr = ...;
incr.value.setChangeCallback((s) -> {
	System.out.println("IncrementalElement changed! "+s.getPrevious()+" -> "+s.getCurrent());
});
```

### Displaying
Once you've created your menu and added all the elements you want, now would probably be a good time to display it.
You can display a menu using `ChatMenu#openFor(Player player)`:
```Java
Player p = ...;
menu.openFor(p);
```

## Links
* [Download](https://www.spigotmc.org/resources/chatmenuapi.45144/)
* [JavaDoc](https://sparse.blue/docs/ChatMenuAPI/index.html)
