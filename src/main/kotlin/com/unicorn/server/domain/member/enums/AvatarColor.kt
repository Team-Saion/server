package com.unicorn.server.domain.member.enums

enum class AvatarColor(val code: String, val hex: String) {
	TEAL_200("Color/teal/200", "#84D6D6"),
	RED_400("Color/red/400", "#F56262"),
	YELLOW_400("Color/yellow/400", "#FFD35C"),
	PURPLE_200("Color/purple/200", "#D595EE"),
	GREEN_200("Color/green/200", "#6DE4B6"),
	ORANGE_400("Color/orange/400", "#FFAB2B"),
	BLUE_200("Color/blue/200", "#8DBEFF"),
	GREY_400("Color/grey/400", "#B1B8B8");

	companion object {
		fun random(): AvatarColor = values().random()
	}
}
