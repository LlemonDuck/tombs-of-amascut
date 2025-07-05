package com.duckblade.osrs.toa.util;

import java.awt.Polygon;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

public class PolygonUtil
{

	public static Polygon rotate(Polygon poly, double thetaRadians)
	{
		double sin = Math.sin(thetaRadians);
		double cos = Math.cos(thetaRadians);

		Polygon ret = new Polygon(new int[poly.npoints], new int[poly.npoints], poly.npoints);
		for (int i = 0; i < poly.npoints; i++)
		{
			double x = poly.xpoints[i];
			double y = poly.ypoints[i];
			ret.xpoints[i] = (int) Math.round(x * cos - y * sin);
			ret.ypoints[i] = (int) Math.round(x * sin + y * cos);
		}
		return ret;
	}

	public static Polygon localPointTranspose(LocalPoint localOrigin, Polygon poly)
	{
		Polygon ret = new Polygon(new int[poly.npoints], new int[poly.npoints], poly.npoints);
		for (int i = 0; i < poly.npoints; i++)
		{
			ret.xpoints[i] = localOrigin.getX() + poly.xpoints[i];
			ret.ypoints[i] = localOrigin.getY() + poly.ypoints[i];
		}
		return ret;
	}

	public static Polygon toCanvas(Client client, WorldView wv, Polygon poly)
	{
		Polygon ret = new Polygon(new int[poly.npoints], new int[poly.npoints], poly.npoints);
		for (int i = 0; i < poly.npoints; i++)
		{
			LocalPoint lp = new LocalPoint(poly.xpoints[i], poly.ypoints[i], wv);
			Point canvas = Perspective.localToCanvas(
				client,
				lp,
				wv.getPlane()
			);

			if (canvas == null)
			{
				return null;
			}
			ret.xpoints[i] = canvas.getX();
			ret.ypoints[i] = canvas.getY();
		}
		return ret;
	}

}
