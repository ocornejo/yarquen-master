package org.yarquen.web.utils;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * 
 * @author Choon-ho Yoon
 * @date Apr 2, 2013
 * @version
 * 
 */
public class YarquenDBUtils {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(YarquenDBUtils.class);
	// FIXME how is this possible
	private static final String MONGO_BIN_DIR = "/home/choonho/Desktop/desarrollo/mongodb-linux-x86_64-2.2.3/bin/";
	private static final String SRC_DIR = "/home/choonho/Desktop/eclipse/workspace/yarquen/yarquen-web/";

	public static void resetDatabase() {
		Mongo mongo;
		try {
			mongo = new Mongo();
			DB db = mongo.getDB("yarquentest");
			db.dropDatabase();
		} catch (UnknownHostException e) {
			LOGGER.debug("Error de host");
		}
	}

	public static void restoreDatabase() {
		Runtime rt = Runtime.getRuntime();
		try {
			LOGGER.debug("restoring database of yarquen test");
			rt.exec(MONGO_BIN_DIR + "mongorestore -d yarquentest --drop "
					+ SRC_DIR + "src/test/resources/yarquentest");
		} catch (IOException e) {
			LOGGER.error("IO Exception when restoring database", e);
			throw new RuntimeException();
		}

	}

}
