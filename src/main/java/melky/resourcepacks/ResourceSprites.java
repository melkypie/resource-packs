/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * Copyright (c) 2018, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package melky.resourcepacks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.SpriteOverride;

/*Formerly TabSprites*/

@RequiredArgsConstructor
public enum ResourceSprites implements SpriteOverride
{
	TAB_BACKGROUND(-201, "/tag-tab.png"),
	TAB_BACKGROUND_ACTIVE(-202, "/tag-tab-active.png"),
	UP_ARROW(-203, "/up-arrow.png"),
	DOWN_ARROW(-204, "/down-arrow.png"),
	NEW_TAB(-205, "/new-tab.png"),

	SPECIAL_BAR_EMPTY(-100, "/special_bar_empty.png"),
	SPECIAL_BAR_FILL(-101, "/special_bar_fill.png"),
	SPECIAL_BAR_FULL(-102, "/special_bar_full.png"),
	SPECIAL_BAR_BORDER(-103, "/special_bar_border.png"),

	SPECIAL_BORDER_TOP_LEFT_CORNER(-1141, "/special_border_top_left_corner.png"),
	SPECIAL_BORDER_TOP(-1142, "/special_border_top.png"),
	SPECIAL_BORDER_TOP_RIGHT_CORNER(-1143, "/special_border_top_right_corner.png"),
	SPECIAL_BORDER_LEFT(-1144, "/special_border_left.png"),
	SPECIAL_BORDER_MIDDLE(-1145, "/special_border_middle.png"),
	SPECIAL_BORDER_RIGHT(-1146, "/special_border_right.png"),
	SPECIAL_BORDER_BOTTOM_LEFT_CORNER(-1147, "/special_border_bottom_left_corner.png"),
	SPECIAL_BORDER_BOTTOM(-1148, "/special_border_bottom.png"),
	SPECIAL_BORDER_BOTTOM_RIGHT_CORNER(-1149, "/special_border_bottom_right_corner.png"),
	BANK_BACKGROUND(-297, "/bank_background.png"),
	BANK_CONTAINER_SEPARATOR_LINE(-897, "/bank_separator_line.png"),

	FIXED_MODE_HIDE_LEFT_BORDER(-206, "/fixed_mode_hide_left.png"),
	FIXED_MODE_HIDE_RIGHT_BORDER(-207, "/fixed_mode_hide_right.png"),
	
	RESIZABLE_VIEWPORT_CLASSIC_BACKGROUND(-208, "/resizable_classic_background.png"),
	RESIZABLE_VIEWPORT_MODERN_BACKGROUND(-209, "/resizable_modern_background.png"),

	EQUIPMENT_VERTICAL_BAR(-172, "/equipment_vertical_bar.png"),
	EQUIPMENT_HORIZONTAL_BAR(-173, "/equipment_horizontal_bar.png"),

	;

	@Getter
	private final int spriteId;

	@Getter
	private final String fileName;
	
}
