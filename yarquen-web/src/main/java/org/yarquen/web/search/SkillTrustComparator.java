package org.yarquen.web.search;

import java.util.Comparator;

import org.yarquen.skill.Skill;



public class SkillTrustComparator implements Comparator<Skill> {

	@Override
	public int compare(Skill o1, Skill o2) {
		// TODO Auto-generated method stub
		 if (o1.getTrust() > o2.getTrust()) return -1;
	     if (o1.getTrust() < o2.getTrust()) return 1;
	     return 0;
		
	}
	
}
