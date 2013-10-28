package org.yarquen.web.pathfinder;

import java.util.List;

import org.yarquen.skill.Skill;

public interface PathFinderService {

	List<Path> find(String accounId, Skill skill);

}
