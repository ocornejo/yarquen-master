package org.yarquen.crawler.scorer.infoq;

import java.util.List;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.scorer.PageScorer;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;
import bixo.operations.BaseScoreGenerator;

/**
 * InfoQ scorer
 * 
 * @author Jorge Riquelme Santana
 * @date 07/08/2012
 * 
 */
public class InfoqScorer extends BaseScoreGenerator implements PageScorer {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(InfoqScorer.class);
	private static final String PLD = "infoq.com";
	private static final long serialVersionUID = 8735367399522397244L;

	@Override
	public double generateScore(String domain, String pld, String url) {
		double score = 0d;
		if (url != null) {
			if (url.contains("." + PLD)) {
				score++;
			}
			if (url.contains("." + PLD + "/articles")) {
				score += 1000;
			}
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("score for link domain={} pld={} url={} is {}",
					new Object[] { domain, pld, url, score });
		}
		return score;
	}

	@Override
	public float getPageScore(ParsedDatum datum, Document doc,
			List<Outlink> outlinks) {
		int score = 0;
		for (Outlink outlink : outlinks) {
			final String url = outlink.getToUrl();
			if (url != null) {
				if (url.contains("." + PLD)) {
					score++;
				}

				if (url.contains("." + PLD + "/articles")) {
					score += 1000;
				}
			}
		}

		LOGGER.trace("score for content url={} is {}", datum.getUrl(), score);
		return score;
	}

	@Override
	public boolean isGoodDomain(String domain, String pld) {
		final boolean igd = pld.equals(PLD);
		LOGGER.trace("is good domain? {} => {}", pld, igd);
		return igd;
	}
}
