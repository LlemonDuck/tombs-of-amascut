package com.duckblade.osrs.toa.util;

import java.awt.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public
enum FontStyle
{
	PLAIN("Plain", Font.PLAIN),
	BOLD("Bold", Font.BOLD),
	ITALIC("Italic", Font.ITALIC);

	private final String name;
	private final int font;

	@Override
	public String toString()
	{
		return name;
	}
}
