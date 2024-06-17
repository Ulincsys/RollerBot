package com.ulincsys.discord.rollerbot;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;

public class DiceObject {
	public static List<Integer> Dimensions = List.of(2, 4, 6, 8, 10, 12, 20);
	Integer dimension;
	Integer value;
	Boolean guilded;
	
	public DiceObject(Integer dimension, Integer value, Boolean guilded) {
		if(!Dimensions.contains(dimension)) {
			throw new IllegalArgumentException(String.format("Dimension [%s] is not supported", dimension));
		}
		
		if(value < 1 || value > dimension) {
			throw new IllegalArgumentException(String.format("Value %s not possible for dimension %s", value, dimension));
		}
		
		this.dimension = dimension;
		this.value = value;
		this.guilded = guilded;
	}
	
	public DiceObject(Integer dimension, Integer value) {
		this(dimension, value, false);
	}
	
	public static List<DiceObject> fromSequence(String sequence) {
		sequence = sequence.strip().toLowerCase();
		
		Integer dimension, amount;
		Boolean guilded = false;
		String[] parts;
		if(sequence.contains("g")) {
			parts = sequence.split("g");
			guilded = true;
			
		} else {
			parts = sequence.split("d");
		}
		
		try {
			if(parts.length == 1) {
				dimension = 6;
				
				amount = Integer.parseInt(parts[0]);
			} else if(parts.length == 2) {
				dimension = Integer.parseInt(parts[1]);
				amount = Integer.parseInt(parts[0]);
			} else {
				throw new RuntimeException(String.format("Could not understand %s as a sequence", sequence));
			}
		} catch(NumberFormatException e) {
			throw new RuntimeException(String.format("Could not parse [%s] as a sequence. Error when trying to parse %s as an integer.", sequence, e.getMessage().split(": ")[1]));
		}
			
		if(!Dimensions.contains(dimension)) {
			throw new IllegalArgumentException(String.format("Dimension [%s] is not supported", dimension));
		}
		
		List<DiceObject> result = new ArrayList<DiceObject>(amount);
		
		for(int i = 0; i < amount; ++i) {
			result.add(random(dimension, guilded));
		}
		
		return result;
	}
	
	public static DiceObject random() {
		Integer d = ThreadLocalRandom.current().nextInt(Dimensions.size());
		return random(Dimensions.get(d));
	}
	
	public static DiceObject random(Integer dimension) {
		return random(dimension, false);
	}
	
	public static DiceObject random(Integer dimension, Boolean guilded) {
		Integer v = ThreadLocalRandom.current().nextInt(dimension) + 1;
		return new DiceObject(dimension, v, guilded);
	}
	
	public String asEmote() {
		String s = toString();
		if(EntryPoint.emojis.containsKey(s)) {
			var emote = EntryPoint.emojis.get(s);
			return emote.asFormat();
		}
		return String.format(":%s:", s);
	}
	
	@Override
	public String toString() {
		if(guilded) {
			return String.format("guilded%s_%s", dimension, value);
		}
		
		return String.format("d%s_%s", dimension, value);
	}
}






























