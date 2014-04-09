package de.myreality.pretender.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.FitViewport;

import de.myreality.pretender.Entity;
import de.myreality.pretender.PretenderGame;
import de.myreality.pretender.Resources;
import de.myreality.pretender.VillagerSpawner;
import de.myreality.pretender.graphics.HouseTextureGenerator;
import de.myreality.pretender.graphics.Renderer;
import de.myreality.pretender.graphics.StreetTextureGenerator;
import de.myreality.pretender.graphics.TextureGenerator;

public class IngameScreen implements Screen {
	
	private static final Color SKY = Color.valueOf(Resources.COLOR_SKY);
	
	private PretenderGame game;
	
	private OrthographicCamera camera;
	
	private Stage stage;
	
	private Renderer renderer;
	
	private Batch batch;
	
	private Pool<Entity> pool;
	
	private Sprite foreground, background;
	
	private Entity street;
	
	private ShaderProgram crtShader;
	
	private VillagerSpawner spawner;
	
	private FrameBuffer buffer;
	
	private long time;
	
	public IngameScreen(PretenderGame game) {
		this.game = game;
		time = 0;
	}

	@Override
	public void render(float delta) {
		
		Gdx.gl.glClearColor(SKY.r, SKY.g, SKY.b, SKY.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		time += delta + 1;
		
		spawner.update(delta);
		
		stage.act(delta);
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		
		batch.setShader(null);
		
		buffer.begin();
		batch.begin();		
			background.draw(batch);
			renderer.render(batch, delta);			
			foreground.draw(batch);
		batch.end();
		batch.flush();
		buffer.end();
		
		batch.setShader(crtShader);
		
		batch.begin();		
			crtShader.setUniformf("time", time);
			crtShader.setUniformf("frequency", 100.0f);
			crtShader.setUniformf("noiseFactor", 0.2f);
			crtShader.setUniformf("intensity", 1.6f);
			crtShader.setUniformf("lineSpeed", 12.5f);
			batch.draw(buffer.getColorBufferTexture(), 0f, 0f);
		batch.end();
		
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		
		if (stage == null) {
			stage = new Stage();
			Gdx.input.setInputProcessor(stage);
		}
		
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		stage.setViewport(new FitViewport(width, height));

		if (buffer != null) {
			buffer.dispose();
		}
		
		buffer = new FrameBuffer(Format.RGBA8888, width, height, false);
	}

	@Override
	public void show() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());			
		camera.setToOrtho(true);
		pool = Pools.get(Entity.class);
		renderer = new Renderer(pool);
		batch = new SpriteBatch();	
		final int BACKHEIGHT = 150;
		
		generateStreet();
		
		foreground = generateHouseRow(street.getY() + street.getHeight(), (int) (Gdx.graphics.getHeight() - (street.getY() + street.getHeight())));
		background = generateHouseRow(street.getY() - BACKHEIGHT, BACKHEIGHT);
		
		crtShader = new ShaderProgram(Gdx.files.internal("crt.vert"), Gdx.files.internal("crt.frag"));		

		spawner = new VillagerSpawner(street, renderer);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
	}
	
	void generateStreet() {
		final float STREET_HEIGHT = 0.4f;
		final float STREET_POS = 0.4f;
		
		street = pool.obtain();
		renderer.add(street);
		TextureGenerator strTexGenerator = new StreetTextureGenerator();
		street.setTexture(strTexGenerator.create(Gdx.graphics.getWidth(), (int) (Gdx.graphics.getHeight() * STREET_HEIGHT)));
		street.setDimensions(Gdx.graphics.getWidth(), (int) (Gdx.graphics.getHeight() * STREET_HEIGHT));
		street.setY(Gdx.graphics.getHeight() * STREET_POS);
	}
	
	Sprite generateHouseRow(float y, int height) {
		// Bottom houses
		TextureGenerator houseTexGenerator = new HouseTextureGenerator();
		final int OFFSET = 70;
		final int MIN_HEIGHT = (int) (Gdx.graphics.getHeight() - (height));
		final int MAX_HEIGHT = (int) (MIN_HEIGHT + OFFSET);
		
		FrameBuffer buffer = new FrameBuffer(Format.RGBA4444, Gdx.graphics.getWidth(), MAX_HEIGHT, false);
		
		Matrix4 projectionMatrix = new Matrix4();
		projectionMatrix.setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());

		batch.setProjectionMatrix(projectionMatrix);
		
		buffer.begin();		
		batch.begin();
		
			int offsetX = (int) -(Math.random() * 250);
			
			while (offsetX < buffer.getWidth()) {
				Texture texture = houseTexGenerator.create((int)Math.round(150 + Math.random() * 50), (int)Math.round(MIN_HEIGHT + Math.random() * OFFSET));
				batch.draw(texture, offsetX, 0f);
				offsetX += texture.getWidth();
			}
			
		batch.end();
		batch.flush();		
		buffer.end();
		
		Sprite s = new Sprite(buffer.getColorBufferTexture());
		s.setY(y - OFFSET);
		return s;
	}

}
