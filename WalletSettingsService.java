/*
 *
 *  * Copyright (c) 2014- MHISoft LLC and/or its affiliates. All rights reserved.
 *  * Licensed to MHISoft LLC under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. MHISoft LLC licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.mhisoft.wallet.service;

import java.awt.Dimension;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JSplitPane;

import com.github.cliftonlabs.json_simple.*;
import org.mhisoft.wallet.SystemSettings;
import org.mhisoft.wallet.model.WalletSettings;

/**
 * Description: service for the settings.
 *
 * @author Tony Xue
 * @since Apr, 2016
 */
public class WalletSettingsService {

	//Enum, welches JsonKey implementiert
	public enum SettingsKeys implements JsonKey {
		FONTSIZE,
		DIMENSION_X,
		DIMENSION_Y,
		DIVIDER_LOCATION,
		LAST_FILE,
		IDLE_TIMEOUT,
		RECENT_FILES,
		TREE_EXPANDED,
		RECENT_OPEN_DIR;

		@Override
		public String getKey() {
			return this.name().toLowerCase();
		}

		@Override
		public Object getValue() {
			//return this.value;
			return null;
		}
	}

	//Settings fileName anpassen, damit .json Dateien verwendet werden können
	public static final String userHome = System.getProperty("user.home") + File.separator;
	public static final String settingsFilePath = userHome + "eVaultSettings.json";


	//user.dir --> app launch dir
	/**
	 * Save the settings to file
	 * @param settings
	 */

	//Erstelle neues JsonObject, ruft writeSettings auf um die zu speichernden Daten zum JsonObject hinzuzufügen
	public void saveSettingsToFile(WalletSettings settings) {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(settingsFilePath))) {
			if (SystemSettings.isDevMode)
				settings.setIdleTimeout(-1);

			JsonObject jsonObject = new JsonObject();
			writeSettings(jsonObject, settings);
			jsonObject.toJson(out);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Gespeicherte Daten mittels Jsoner.deserialize einlesen, ruft setSettingsFromJsonObject auf, um die Daten in das Settings
	//Object zu speichern
	public WalletSettings readSettingsFromFile() {
			try(FileReader fileReader = new FileReader(settingsFilePath)) {

				JsonObject jsonObject = (JsonObject) Jsoner.deserialize(fileReader);
				WalletSettings settings = setSettingsFromJsonObject(jsonObject);
				ServiceRegistry.instance.registerSingletonService(settings);

				return settings;
			} catch (JsonException | IOException e) {
				 throw new RuntimeException(e);
			}
	}

	//Writing the settings in a json file
	private static void writeSettings(JsonObject jsonObject, WalletSettings settings) {
		jsonObject.put(SettingsKeys.FONTSIZE, settings.getFontSize());
		jsonObject.put(SettingsKeys.DIMENSION_X, settings.getDimensionX());
		jsonObject.put(SettingsKeys.DIMENSION_Y, settings.getDimensionY());
		jsonObject.put(SettingsKeys.DIVIDER_LOCATION, settings.getDividerLocation());
		jsonObject.put(SettingsKeys.LAST_FILE, settings.getLastFile());
		jsonObject.put(SettingsKeys.IDLE_TIMEOUT, settings.getIdleTimeout());
		jsonObject.put(SettingsKeys.RECENT_FILES, settings.getRecentFiles());
		jsonObject.put(SettingsKeys.TREE_EXPANDED, settings.isTreeExpanded());
		jsonObject.put(SettingsKeys.RECENT_OPEN_DIR, settings.getRecentOpenDir());
	}

	//Get the values and set them in the settings object
	public WalletSettings setSettingsFromJsonObject(JsonObject jsonObject){
		WalletSettings settings = new WalletSettings();

		settings.setFontSize(jsonObject.getInteger(SettingsKeys.FONTSIZE));
		settings.setDimensionX(jsonObject.getInteger(SettingsKeys.DIMENSION_X));
		settings.setDimensionY(jsonObject.getInteger(SettingsKeys.DIMENSION_Y));
		settings.setDividerLocation(jsonObject.getDouble(SettingsKeys.DIVIDER_LOCATION));
		settings.setLastFile(jsonObject.getString(SettingsKeys.LAST_FILE));
		settings.setIdleTimeout(jsonObject.getLong(SettingsKeys.IDLE_TIMEOUT));
		settings.setTreeExpanded(jsonObject.getBoolean(SettingsKeys.TREE_EXPANDED));
		settings.setRecentOpenDir(jsonObject.getString(SettingsKeys.RECENT_OPEN_DIR));
		return settings;
	}


	public void updateAndSavePreferences() {
		//save the settings
		Dimension d = ServiceRegistry.instance.getWalletForm().getFrame().getSize();
		WalletSettings settings = ServiceRegistry.instance.getWalletSettings();
		settings.setDimensionX(d.width);
		settings.setDimensionY(d.height);

		//calculate proportion
		JSplitPane split = ServiceRegistry.instance.getWalletForm().getSplitPanel();
		double p = Double.valueOf(split.getDividerLocation()).doubleValue() / Double.valueOf(split.getWidth() - split.getDividerSize());
		settings.setDividerLocation(Double.valueOf(p * 100 + 0.5).intValue() / Double.valueOf(100));

		ServiceRegistry.instance.getWalletSettingsService().saveSettingsToFile(settings);
	}
}
