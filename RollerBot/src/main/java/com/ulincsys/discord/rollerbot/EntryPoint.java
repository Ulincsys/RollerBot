package com.ulincsys.discord.rollerbot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.reactivestreams.Publisher;

import java.io.BufferedReader;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public class EntryPoint {
	public static GatewayDiscordClient client;
	public static Long gameServerID;
	public static Map<String, GuildEmoji> emojis;
	
	static {
		emojis = new HashMap<String, GuildEmoji>();
	}

	public static void main(String[] args) throws IOException {
		var reader = new BufferedReader(new FileReader(new File("secret.txt")));
		
		String key = reader.readLine();
		
		client = DiscordClientBuilder.create(key).build()
		        .login()
		        .block();
		gameServerID = Long.valueOf(reader.readLine());
		
		reader.close();
		
		long applicationId = client.getRestClient().getApplicationId().block();

		// Build the /roll command definition
		ApplicationCommandRequest rollCmdRequest = ApplicationCommandRequest.builder()
		    .name("roll")
		    .description("Roll some dice")
		    .addOption(ApplicationCommandOptionData.builder()
		        .name("sequence")
		        .description("IE: \"2d6\" or \"3d8,6d12\". Default: \"1d6\"")
		        .type(ApplicationCommandOption.Type.STRING.getValue())
		        .required(false)
		        .build()
		    ).build();
		
		// Register the event handler
		client.on(ChatInputInteractionEvent.class, event -> handleEvent(event)).subscribe();

		// Create the command with Discord
		client.getRestClient().getApplicationService()
		    .createGuildApplicationCommand(applicationId, gameServerID, rollCmdRequest)
		    .subscribe();
		
		// Build the emoji map
		var emojiList = client.getGuildEmojis(Snowflake.of(gameServerID)).buffer().blockFirst();
		emojiList.forEach(e -> emojis.put(e.getName(), e));
		
		// Block indefinitely on connection
		client.onDisconnect().block();
	}
	
	public static Publisher<Void> handleEvent(ChatInputInteractionEvent event) {
		String commandName = event.getCommandName();
		String username = event.getInteraction().getUser().getUsername();
		
		System.out.println("Command from: " + username);
		
		switch(Commands.valueOf(commandName.toUpperCase())) {
			case ROLL: {
				var sequence = event.getOption("sequence").orElse(null);
				
				if(sequence == null) {
					return event.reply(DiceObject.random(6).asEmote());
				} else {
					String s = sequence.getValue().get().asString();
					StringBuilder response = new StringBuilder();
					
					for(String part : s.split(",")) {
						try {
							response.append(String.join("", DiceObject.fromSequence(part).stream().map(x -> x.asEmote()).toList()));
						} catch(Exception e) {
							return event.reply(String.format("ERROR: %s", e.getMessage()));
						}
						response.append("\n");
					}
					return event.reply(response.toString());
				}
			}
			default:
				break;
		}
		System.out.println("BAD NEWS BEARS");
		return Mono.<Void>empty();
	}
}

































