package de.myreality.pretender;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import de.myreality.pretender.graphics.RenderAnimationStrategy;
import de.myreality.pretender.graphics.Renderer;
import de.myreality.pretender.graphics.TexturePool;
import de.myreality.pretender.graphics.VillagerTextureGenerator;
import de.myreality.pretender.tweens.EntityTween;

public class VillagerSpawner {
	
	public static final float SPAWN_RATE = 0.5f;
	public static final float FRAME_DURATION = 0.5f;
	
	public static final int INITIAL_RATE = 400;
	
	private static final int TEXTURE_CAPACITY = 50;
	
	private static final int VILLAGER_WIDTH = 25;
	private static final int VILLAGER_HEIGHT = 32;
	
	private Entity street;
	
	private Renderer renderer;
	
	private Pool<Entity> entityPool;
	
	private TexturePool texturePool;
	
	private float time;
	
	private TweenManager tweenManager;
	
	public VillagerSpawner(Entity street, Renderer renderer, TweenManager tweenManager) {
		this.street = street;
		this.renderer = renderer;
		
		texturePool = new TexturePool(TEXTURE_CAPACITY, new VillagerTextureGenerator());
		texturePool.setTextureSize(VILLAGER_WIDTH * 2, VILLAGER_HEIGHT * 2);
		this.tweenManager = tweenManager;
		
		entityPool = Pools.get(Entity.class);
		
		for (int i = 0; i < INITIAL_RATE; ++i) {
			spawn((float)(street.getX() + street.getWidth() / 2f + (street.getWidth() / 2f) * Math.random()), (float) (street.getY() + street.getHeight() * Math.random()));
		}
	}

	public void update(float delta) {
		
		time += delta;
		
		if (time >= SPAWN_RATE) {
			time = time - SPAWN_RATE;
			spawn();
		}
	}
	
	void spawn() {
		spawn(street.getX() + street.getWidth() - VILLAGER_WIDTH, (float) (street.getY() + street.getHeight() * Math.random()));
	}
	
	void spawn(float x, float y) {
		Entity villager = entityPool.obtain();
		villager.setPosition(x, y);
		villager.setDimensions(VILLAGER_WIDTH, VILLAGER_HEIGHT);
		villager.setBody(new Rectangle(0, VILLAGER_HEIGHT - VILLAGER_HEIGHT / 3, VILLAGER_WIDTH, VILLAGER_HEIGHT / 3));
		villager.setTexture(texturePool.get());
		RenderAnimationStrategy str = new RenderAnimationStrategy(VILLAGER_WIDTH, VILLAGER_HEIGHT, FRAME_DURATION);
		villager.setRenderStrategy(str);
		villager.setBehavior(new VillagerBehavior(str.getInitialDelay()));		
		renderer.add(villager);
	}
	
	
	class VillagerBehavior implements EntityBehavior {
		
		private float delay;
		
		private float time;
		
		private float waitTime;
		
		public VillagerBehavior(float delay) {
			this.delay = delay;
			time = delay;
			waitTime = (float) (1f + Math.random() * 4f);
		}

		@Override
		public void behave(float delta, Entity entity) {
			
			time += delta;
			
			if (!tweenManager.containsTarget(entity) && time >= waitTime) {
				
				time = 0;
				waitTime = (float) (1f + Math.random() * 4f);
				float xSpeed = (float) (10.0f + Math.random() * 5f);
				
				Tween.to(entity, EntityTween.POS_X, FRAME_DURATION * 2)
					.target(entity.getX() - xSpeed)
					.ease(TweenEquations.easeInOutCubic)
					.start(tweenManager);
				
				float speed = 8.0f;
				
				float factor = Math.random() > 0.5f ? speed : -speed;
				
				float newPos = entity.getY() + factor;
				
				if (newPos + entity.getBody().getY() <= street.getY()) {
					newPos = entity.getY() + speed;
				}
				
				if (newPos + entity.getHeight() >= street.getY() + street.getHeight()) {
					newPos = entity.getY() - speed;
				}
				
				Tween.to(entity, EntityTween.POS_Y, FRAME_DURATION * 2)
				.target(newPos)
				.ease(TweenEquations.easeInOutCubic)
				.start(tweenManager);
				
				delay = 0f;
			}
		}
		
	}
	
	
}
