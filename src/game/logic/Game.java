package game.logic;

import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

import game.logic.Dragon.DragonBehavior;
import game.logic.Labyrinth.Symbols;
import game.logic.LivingBeing.Type;

/**
 * 
 * Represents a Game.
 * 
 * @author Henrique Ferrolho
 * @see MazeBuilder
 * @see Labyrinth
 * @see LivingBeing
 * @see Dragon
 * @see Eagle
 * @see Hero
 * @see Sword
 * 
 */
public class Game implements Serializable {
	private static final long serialVersionUID = 1L;
	private MazeBuilder mazeBuilder = new MazeBuilder();
	private Labyrinth lab;
	private Vector<LivingBeing> livingBeings;
	private Hero hero;
	private Eagle eagle;
	private Sword sword;
	private boolean exitIsOpen = false;

	public Game(DragonBehavior dragonBehavior, int numDragons) {
		// initializing variables
		lab = mazeBuilder.buildDemo();

		initializeVariables(dragonBehavior, numDragons);
	}

	public Game(int width, int height, DragonBehavior dragonBehavior,
			int numDragons) {
		// initializing variables
		lab = mazeBuilder.build(width, height);

		initializeVariables(dragonBehavior, numDragons);
	}

	public void initializeVariables(DragonBehavior dragonBehavior,
			int numDragons) {
		livingBeings = new Vector<LivingBeing>();

		// creating hero
		livingBeings.add(new Hero("Hero", lab));
		hero = (Hero) livingBeings.get(0);

		// creating eagle
		livingBeings.add(new Eagle());
		eagle = (Eagle) livingBeings.get(1);
		eagle.setPosition(new Coord(hero.getPosition()));

		// creating sword
		sword = new Sword(lab, livingBeings.get(0));

		// summoning dragons
		for (int i = 0; i < numDragons; i++)
			livingBeings.add(new Dragon(dragonBehavior, lab, livingBeings,
					sword));
	}

	public boolean updateGame(Direction directionToMoveHero) {
		boolean done = false;

		// moving living beings
		for (LivingBeing i : livingBeings) {
			Random r = new Random();

			if (!i.isMoving()) {
				// i.setMoving(true);

				if (i.getType() == Type.HERO)
					// moving hero
					i.move(lab, directionToMoveHero);
				else {
					// move dragon randomly
					i.move(lab, Direction.values()[r.nextInt(4)]);
				}
			}
		}

		// updating eagle
		eagle.update(lab, livingBeings, hero, sword);

		// checking if player got sword
		hero.catchSwordIfPossible(sword);

		// if hero is next to a dragon
		for (LivingBeing i : livingBeings) {
			// skipping if comparing to something other than a dragon
			if (i.getType() != Type.DRAGON)
				continue;

			// if dragon is not dead nor sleeping
			if (!i.isDead() && hero.distanceTo(i) <= 1) {
				// if hero has sword
				if (hero.hasSword()) {
					// kill the dragon
					i.kill();

					// activate hero flag
					hero.killedADragon();
				} else if (!i.isSleeping()) {
					// if hero has no sword and dragon is not sleeping
					hero.kill();

					// killing eagle if it is with hero
					if (hero.hasEagle())
						eagle.kill();

					done = true;
				}
			}
		}

		// updating exit state
		if (hero.hasKilledADragon())
			exitIsOpen = true;

		// if hero is able to step on Exit, game is done
		if (lab.getLab()[hero.getPosition().getY()][hero.getPosition().getX()] == Symbols.EXIT)
			done = true;

		return done;
	}

	public Labyrinth getLabyrinth() {
		return lab;
	}

	public Hero getHero() {
		return hero;
	}

	public Vector<LivingBeing> getLivingBeings() {
		return livingBeings;
	}

	public Sword getSword() {
		return sword;
	}

	public Eagle getEagle() {
		return eagle;
	}

	public boolean exitIsOpen() {
		return exitIsOpen;
	}

	@Override
	public String toString() {
		return new StringBuffer("Labyrinth: ").append(lab)
				.append("Living Beings: ").append(livingBeings)
				.append("Hero: ").append(hero).append("Eagle: ").append(eagle)
				.append("Sword: ").append(sword).toString();
	}
}
