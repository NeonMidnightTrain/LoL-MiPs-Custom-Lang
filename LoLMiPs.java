package mars.mips.instructions.customlangs;
    import mars.simulator.*;
    import mars.mips.hardware.*;
    import mars.mips.instructions.syscalls.*;
    import mars.*;
    import mars.util.*;
    import java.util.*;
    import java.io.*;
    import mars.mips.instructions.*;
    import java.util.Random;


public class LoLMiPs extends CustomAssembly{
    @Override
    public String getName(){
        return "LoL Lang";
    }

    @Override
    public String getDescription(){
        return "Welcome to the League of Legends.";
    }

    @Override
    protected void populate(){

      instructionList.add(
                new BasicInstruction("tp target", 
            	 "Teleport unconditionally : Teleport to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000010 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processJump(
                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2)));            
                  }
               }));
         instructionList.add(
                new BasicInstruction("ret $t1", 
            	 "Return register unconditionally : Return to statement whose address is in $t1",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 fffff 00000 00000 00000 001000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processJump(RegisterFile.getValue(operands[0]));
                  }
               }));
         instructionList.add(
                new BasicInstruction("port target",
                "Portal and link : Set $ra to Program Counter (return address) then portal to statement at target address",
            	 BasicInstructionFormat.J_FORMAT,
                "000011 ffffffffffffffffffffffffff",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     Globals.instructionSet.processReturnAddress(31);// RegisterFile.updateRegister(31, RegisterFile.getProgramCounter());
                     Globals.instructionSet.processJump(
                        (RegisterFile.getProgramCounter() & 0xF0000000)
                                | (operands[0] << 2));
                  }
               }));
          instructionList.add(
                new BasicInstruction("pick $t1,-100($t2)",
            	 "Pick player : Set $at to value to update",
                BasicInstructionFormat.I_FORMAT,
                "100011 ttttt fffff ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try
                     {
                        RegisterFile.updateRegister(operands[0],
                            Globals.memory.getWord(
                            RegisterFile.getValue(operands[2]) + operands[1]));
                     } 
                         catch (AddressErrorException e)
                        {
                           throw new ProcessingException(statement, e);
                        }
                  }
               }));
	   instructionList.add(
                new BasicInstruction("amt $t1,$t2,-100",
            	 "Add or substract immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)",
                BasicInstructionFormat.I_FORMAT,
                "001000 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = operands[2] << 16 >> 16;
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));
            instructionList.add(
                new BasicInstruction("check $t1,$t2,label",
                "Check if not equal : Branch to statement at label's address if $t1 and $t2 are not equal",
            	 BasicInstructionFormat.I_BRANCH_FORMAT,
                "000101 fffff sssss tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     if (RegisterFile.getValue(operands[0])
                        != RegisterFile.getValue(operands[1]))
                     {
                        Globals.instructionSet.processBranch(operands[2]);
                     }
                  }
               }));
            instructionList.add(
                new BasicInstruction("pay $t1,$t2,$t3",
                "Set less than : If player $t3 has more gold than item $t2, then set $t1 to 1 else set $t1 to 0",
            	 BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     RegisterFile.updateRegister(operands[0],
                        (RegisterFile.getValue(operands[1])
                        < RegisterFile.getValue(operands[2]))
                                ? 1
                                : 0);
                  }
               }));
            instructionList.add(
                new BasicInstruction("save $t1,-100($t2)",
                "Save changes : save HP or gold to player $t2",
            	 BasicInstructionFormat.I_FORMAT,
                "101011 ttttt fffff ssssssssssssssss",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     try
                     {
                        Globals.memory.setWord(
                            RegisterFile.getValue(operands[2]) + operands[1],
                            RegisterFile.getValue(operands[0]));
                     } 
                         catch (AddressErrorException e)
                        {
                           throw new ProcessingException(statement, e);
                        }
                  }
               }));
            instructionList.add(
                new BasicInstruction("lose $t1,$t2,$t3",
            	 "Lose gold/health : set $t1 to ($t2 minus $t3)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100010",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int sub1 = RegisterFile.getValue(operands[1]);
                     int sub2 = RegisterFile.getValue(operands[2]);
                     int dif = sub1 - sub2;
                  // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
                     if ((sub1 >= 0 && sub2 < 0 && dif < 0)
                        || (sub1 < 0 && sub2 >= 0 && dif >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], dif);
                  }
               }));
            instructionList.add(
                new BasicInstruction("get $t1,$t2,$t3",
            	 "Get gold/health : set $t1 to ($t2 plus $t3)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int add1 = RegisterFile.getValue(operands[1]);
                     int add2 = RegisterFile.getValue(operands[2]);
                     int sum = add1 + add2;
                  // overflow on A+B detected when A and B have same sign and A+B has other sign.
                     if ((add1 >= 0 && add2 >= 0 && sum < 0)
                        || (add1 < 0 && add2 < 0 && sum >= 0))
                     {
                        throw new ProcessingException(statement,
                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                     }
                     RegisterFile.updateRegister(operands[0], sum);
                  }
               }));

            instructionList.add(
                new BasicInstruction("mel $t1",
            	 "Melee minion: player in $t1 gains +21 gold",
                BasicInstructionFormat.I_FORMAT,
                "010000 fffff 00000 0000000000000000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int playerReg = operands[0];
		     int base = RegisterFile.getValue(playerReg);
  		     int goldAddr = base + 4;
                     try {
                        int gold = Globals.memory.getWord(goldAddr);
			gold += 21;
			Globals.memory.setWord(goldAddr, gold);
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));

            instructionList.add(
                new BasicInstruction("ran $t1",
            	 "Ranged minion: player in $t1 gains +14 gold",
                BasicInstructionFormat.I_FORMAT,
                "010001 fffff 00000 0000000000000000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int playerReg = operands[0];
		     int base = RegisterFile.getValue(playerReg);
  		     int goldAddr = base + 4;
                     try {
                        int gold = Globals.memory.getWord(goldAddr);
			gold += 14;
			Globals.memory.setWord(goldAddr, gold);
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));

            instructionList.add(
                new BasicInstruction("can $t1",
            	 "Cannon minion: player in $t1 gains +90 gold",
                BasicInstructionFormat.I_FORMAT,
                "010010 fffff 00000 0000000000000000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int playerReg = operands[0];
		     int base = RegisterFile.getValue(playerReg);
  		     int goldAddr = base + 4;
                     try {
                        int gold = Globals.memory.getWord(goldAddr);
			gold += 90;
			Globals.memory.setWord(goldAddr, gold);
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));
	
            instructionList.add(
                new BasicInstruction("b $t1",
            	 "Recall: restore health for player in $t1 to 500",
                BasicInstructionFormat.I_FORMAT,
                "010011 fffff 00000 0000000000000000",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int playerReg = operands[0];
		     int base = RegisterFile.getValue(playerReg);
  		     int hpAddr = base + 0;
                     try {
			Globals.memory.setWord(hpAddr, 500);
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));

            instructionList.add(
                new BasicInstruction("poke $t1,$t2,-100",
            	 "Deal immediate damage to player in $t1",
                BasicInstructionFormat.I_FORMAT,
                "010100 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int playerReg = operands[0];
		     int imm = operands[2] << 16 >> 16;

		     int base = RegisterFile.getValue(playerReg);
		     int hpAddr = base + 0;
                     try {
			int hp = Globals.memory.getWord(hpAddr);
			hp -= imm;
			Globals.memory.setWord(hpAddr, hp);
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));

            instructionList.add(
                new BasicInstruction("heal $t1,$t2,-100",
            	 "Heal player in $t1 by immediate amount",
                BasicInstructionFormat.I_FORMAT,
                "010101 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int playerReg = operands[0];
		     int imm = operands[2] << 16 >> 16;

		     int base = RegisterFile.getValue(playerReg);
		     int hpAddr = base + 0;
                     try {
			int hp = Globals.memory.getWord(hpAddr);
			hp += imm;
			Globals.memory.setWord(hpAddr, hp);
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));

            instructionList.add(
                new BasicInstruction("buyi $t1,$t2,-100",
            	 "Buy item with gold: if Gold >= cost, substract it",
                BasicInstructionFormat.I_FORMAT,
                "010110 sssss fffff tttttttttttttttt",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int playerReg = operands[0];
		     int imm = operands[2] << 16 >> 16;

		     int base = RegisterFile.getValue(playerReg);
		     int goldAddr = base + 4;
                     try {
			int gold = Globals.memory.getWord(goldAddr);
			if (gold >= imm) {
			  gold -= imm;
			  Globals.memory.setWord(goldAddr, gold);
			}
			
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));

            instructionList.add(
                new BasicInstruction("kill $t1,$t2,$t3",
            	 "Kill: set victim $t2 HP to 0 and give killer $t1 300 gold",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 110001",
                new SimulationCode()
               {
                   public void simulate(ProgramStatement statement) throws ProcessingException
                  {
                     int[] operands = statement.getOperands();
                     int killerReg = operands[0];
		     int victimReg = operands[1];

		     int killerBase = RegisterFile.getValue(killerReg);
		     int victimBase = RegisterFile.getValue(victimReg);

		     int victimHpAddr = victimBase + 0;
		     int killerGoldAddr = killerBase + 4;


                     try {
			Globals.memory.setWord(victimHpAddr, 0);

			int gold = Globals.memory.getWord(killerGoldAddr);
			gold += 300;
			Globals.memory.setWord(killerGoldAddr, gold);
                     } catch (AddressErrorException e) {
  		        throw new ProcessingException(statement, e);
		     }
                  }
               }));

        // INITLOL : initialize all player stats and base addresses
        instructionList.add(
            new BasicInstruction("initlol",
                "Initialize players: set $t0-$t9 to player structs and give 500 HP/500 GOLD",
                BasicInstructionFormat.R_FORMAT,
                "010111 00000 00000 00000 00000 000000",  // custom encoding
                new SimulationCode() {
                    public void simulate(ProgramStatement statement) throws ProcessingException {
                        int playerBase = 0x10008000; // base address for player 0
                        int playerSize = 8;          // 2 words: 0=HP, 4=GOLD

                        int baseHP   = 500;
                        int baseGold = 500;

                        int T0 = 8;  // register number of $t0

                        try {
                            for (int i = 0; i < 10; i++) {
                                int base = playerBase + i * playerSize;

                                // Set $t0..$t9 to player structs
                                RegisterFile.updateRegister(T0 + i, base);

                                // Initialize HEALTH and GOLD in memory
                                Globals.memory.setWord(base + 0, baseHP);
                                Globals.memory.setWord(base + 4, baseGold);
                            }
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(statement, e);
                        }
                    }
                })
        );


	     try {
		LoLRuntime.initLoLWorld();
	     } catch (AddressErrorException e) {

	     }
	}
}