# Matchbooks

Matchbooks is a library which adds custom ingredient matchers to recipes.

This functionality originated from [Incubus Core.](https://github.com/Azzyypaaras/Incubus-Core)

## Setup

Include Matchbooks via Jitpack:

```gradle
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
		
	dependencies {
	        implementation 'com.github.Noaaan:Matchbooks:1.20-SNAPSHOT'
	}
```

## Usage

For usage refer to the GitHub Wiki: https://github.com/Noaaan/Matchbooks/wiki

Here is the list of currently supported matches:
- Int
- IntRange
- Float
- Long
- Short
- Byte
- String
- Boolean
- Enchantment