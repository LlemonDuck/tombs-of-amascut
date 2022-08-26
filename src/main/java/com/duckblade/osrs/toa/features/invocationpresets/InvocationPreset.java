package com.duckblade.osrs.toa.features.invocationpresets;

import com.duckblade.osrs.toa.util.Invocation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Value;

@Value
public class InvocationPreset
{

	public static InvocationPreset parse(String serialized)
	{
		String[] parts = serialized.trim().split(";");
		if (parts.length != 2)
		{
			throw new IllegalArgumentException("Invalid format");
		}

		String name = parts[0];
		Set<Invocation> invocations = Arrays.stream(parts[1].split(","))
			.map(Invocation::valueOf)
			.collect(Collectors.toSet());

		return new InvocationPreset(name, invocations);
	}

	private final String name;
	private final Set<Invocation> invocations;

	public String serialize()
	{
		return name + ";" + invocations.stream().map(Invocation::name).collect(Collectors.joining(","));
	}

	public int getRaidLevel()
	{
		return invocations.stream()
			.mapToInt(Invocation::getRaidLevel)
			.sum();
	}

}
