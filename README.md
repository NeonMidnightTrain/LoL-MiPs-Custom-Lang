# LoL-MiPs-Custom-Lang
This is a custom Mars assembly language that is built based on the game League of Legends

It has 10 basic-instructions and 8-unique instructions.

Structure of language: 
Uses 10 $t registers to store players data (10 players in a game) 
LoLMiPs uses offsets to store hp and gold values (0($) and 4($) respectively)
All $t register(players) will be loaded with 500 hp and gold at the start of program
(you must type "initlol" before any code)

10 basic instructions are renames of existing mips instructions:
tp target : j target
port target : jal target
ret $reg : jr $reg
pick $reg, offset($reg) : lw $reg, offset($reg)
save $reg, offset($treg) : sw $reg, offset($reg)
amt $reg, $reg, imm : addi $reg, $reg, imm
get $reg, $reg, $reg : add $reg, $reg, $reg
lose $reg, $reg, $reg : sub $reg, $reg, $reg
pay $reg, $reg, $reg : slt $reg, $reg, $reg
check $reg, $reg, label : bne $reg, $reg, label

8 unique instructions mimics LoL game mechanics:
mel, ran, can; are player $ killing a minion and gaining 21, 14, 90 gold.
b is for recall; b $ will set player's health back to 500
poke/heal $; subtract or add hp to player $
buy; allows player to spend gold on items
kill; allows player $1 to kill $2, $2 hp goes to 0 and $1 gains 300 gold

//How to run LoLMiPs on marsLE
This is built on a mac so there is only mac instruction.
Recommend doing all this in terminal for best chance at success:
1. Change directory to the MARS-LE-main Folder.
2. Build custom lang: java -jar BuildCustomLang.jar LoLMiPs.java
3. Run mars: java -jar mars.jar
4. Tool > Language Switcher > Select Language > LoL Lang
5. Have fun!
