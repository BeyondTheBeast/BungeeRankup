package com.Vankka.bungeerankup;

public class Timer implements Runnable
{
	@Override
	public void run() {
		BungeeRankup.getInstance().check();
	}
}
