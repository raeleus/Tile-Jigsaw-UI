package com.ray3k.tilejigsawui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.Hinting;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import static com.ray3k.tilejigsawui.Core.Scene.MENU;

public class Core extends ApplicationAdapter {
    private Stage stage;
    private Skin skin;
    private AssetManager manager;
    public static enum Scene {
        MENU, GAME, SETTINGS, CREDITS, SETUP
    }
    private Scene scene;
    private float density;

    @Override
    public void create() {
        density = (Gdx.graphics.getDensity() + .4f);

        load();
        
        transition(Scene.MENU);
    }
    
    private void load() {
        Resolution[] resolutions = {new Resolution(480, 480, "tile-jigsaw-ui/skin 1x"), new Resolution(720, 720, "tile-jigsaw-ui/skin 2x"), new Resolution(1080, 1080, "tile-jigsaw-ui/skin 3x")};
        ResolutionFileResolver resolver = new ResolutionFileResolver(new InternalFileHandleResolver(), resolutions);
        manager = new AssetManager(resolver);
        manager.setLoader(TextureAtlas.class, new TextureAtlasLoader(resolver));
        manager.load("tile-jigsaw-ui.atlas", TextureAtlas.class);
        manager.finishLoading();

        skin = new Skin(Gdx.files.internal("tile-jigsaw-ui/tile-jigsaw-ui.json"), manager.get("tile-jigsaw-ui.atlas", TextureAtlas.class)) {
            //Override json loader to process FreeType fonts from skin JSON
            @Override
            protected Json getJsonLoader(final FileHandle skinFile) {
                Json json = super.getJsonLoader(skinFile);
                final Skin skin = this;

                json.setSerializer(FreeTypeFontGenerator.class, new Json.ReadOnlySerializer<FreeTypeFontGenerator>() {
                    @Override
                    public FreeTypeFontGenerator read(Json json,
                            JsonValue jsonData, Class type) {
                        String path = json.readValue("font", String.class, jsonData);
                        jsonData.remove("font");

                        Hinting hinting = Hinting.valueOf(json.readValue("hinting", 
                                String.class, "AutoMedium", jsonData));
                        jsonData.remove("hinting");

                        TextureFilter minFilter = TextureFilter.valueOf(
                                json.readValue("minFilter", String.class, "Nearest", jsonData));
                        jsonData.remove("minFilter");

                        TextureFilter magFilter = TextureFilter.valueOf(
                                json.readValue("magFilter", String.class, "Nearest", jsonData));
                        jsonData.remove("magFilter");

                        FreeTypeFontParameter parameter = json.readValue(FreeTypeFontParameter.class, jsonData);
                        parameter.hinting = hinting;
                        parameter.minFilter = minFilter;
                        parameter.magFilter = magFilter;
                        
                        //These lines override skin values
                        parameter.size = Math.max(MathUtils.round(density * parameter.size), 5);
                        parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\\\"!`?'.,;:()[]{}<>|/@\\\\^$€-%+=#_&~* ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ•ЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюяѐёђѓєѕіїјљњћќѝўџѠѡѢѣѤѥѦѧѨѩѪѫѬѭѮѯѰѱѲѳѴѵѶѷѸѹѺѻѼѽѾѿҀҁ҂҃҄҅҆҇҈҉ҊҋҌҍҎҏҐґҒғҔҕҖҗҘҙҚқҜҝҞҟҠҡҢңҤҥҦҧҨҩҪҫҬҭҮүҰұҲҳҴҵҶҷҸҹҺһҼҽҾҿӀӁӂӃӄӅӆӇӈӉӊӋӌӍӎӏӐӑӒӓӔӕӖӗӘәӚӛӜӝӞӟӠӡӢӣӤӥӦӧӨөӪӫӬӭӮӯӰӱӲӳӴӵӶӷӸӹӺӻӼӽӾӿ";
                        
                        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(skinFile.parent().child(path));
                        BitmapFont font = generator.generateFont(parameter);
                        skin.add(jsonData.name, font);
                        if (parameter.incremental) {
                            generator.dispose();
                            return null;
                        } else {
                            return generator;
                        }
                    }
                });

                return json;
            }
        };

        TiledDrawable tiledDrawable = (TiledDrawable) skin.getDrawable("slider-tiled");
        tiledDrawable.setMinWidth(tiledDrawable.getRegion().getRegionWidth());
        tiledDrawable.setMinHeight(tiledDrawable.getRegion().getRegionHeight());

        tiledDrawable = (TiledDrawable) skin.getDrawable("slider-before-knob-tiled");
        tiledDrawable.setMinWidth(0);
        tiledDrawable.setMinHeight(tiledDrawable.getRegion().getRegionHeight());
        
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        populate();
    }
    
    private void transition(Scene scene) {
        if (this.scene != scene) {
            this.scene = scene;

            if (stage.getRoot().getChildren().size == 0) {
                populate();
            } else {
                Gdx.input.setInputProcessor(null);
                Table oldRoot = (Table) stage.getRoot().getChildren().first();
                oldRoot.addAction(Actions.sequence(Actions.fadeOut(.25f), new Action() {
                    @Override
                    public boolean act(float delta) {
                        populate();
                        Gdx.input.setInputProcessor(stage);
                        return true;
                    }
                }, Actions.removeActor()));
            }
        }
    }
    
    private void populate() {
        if (scene != null) {
            Table root = new Table();
            root.setBackground(skin.getDrawable("white"));
            root.setColor(35 / 255f, 57 / 255f, 91 / 255f, 1);
            root.setFillParent(true);
            stage.addActor(root);
            
            switch (scene) {
                case MENU:
                    Table table = generateMenuTable();
                    root.add(table).growY();

                    table = new Table();
                    ScrollPane scrollPane = new ScrollPane(table, skin);
                    root.add(scrollPane).grow();

                    table.defaults().space(10 * density);
                    for (int i = 0; i < 8; i++) {
                        table.row();
                        Button button = new Button(skin, "large");
                        button.pad(10);
                        table.add(button);
                        button.addListener(new ChangeListener() {
                            @Override
                            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                                transition(Scene.SETUP);
                            }
                        });

                        button.defaults().space(10);
                        Image image = new Image(skin, "white");
                        button.add(image).minSize(130).grow();

                        button.row();
                        Label label = new Label("Solved: 0\nTime challenge best: 0", skin);
                        label.setAlignment(Align.center);
                        button.add(label);
                    }
                    break;
                case SETTINGS:
                    table = generateMenuTable();
                    root.add(table).growY();
                    
                    table = new Table();
                    root.add(table).grow();
                    
                    Table subTable = new Table();
                    subTable.setBackground(skin.getDrawable("table-bg"));
                    subTable.setColor(43 / 255f, 70 / 255f, 109 / 255f, 1);
                    subTable.pad(50);
                    table.add(subTable);
                    
                    subTable.defaults().space(30);
                    Label label = new Label("Language", skin, "menu");
                    subTable.add(label).right();
                    
                    SelectBox<String> selectBox = new SelectBox<String>(skin);
                    selectBox.setItems("English", "Español", "Pyccкий");
                    selectBox.setAlignment(Align.center);
                    selectBox.getList().setAlignment(Align.center);
                    subTable.add(selectBox).growX();
                    
                    subTable.row();
                    label = new Label("Music", skin, "menu");
                    subTable.add(label).right();
                    
                    Slider slider = new Slider(0, 100, 1, false, skin);
                    subTable.add(slider).size(slider.getStyle().background.getMinWidth(), slider.getStyle().background.getMinHeight());
                    
                    subTable.row();
                    label = new Label("Sound", skin, "menu");
                    subTable.add(label).right();
                    
                    slider = new Slider(0, 100, 1, false, skin);
                    subTable.add(slider).size(slider.getStyle().background.getMinWidth(), slider.getStyle().background.getMinHeight());
                    
                    break;
                case CREDITS:
                    table = generateMenuTable();
                    root.add(table).growY();

                    table = new Table();
                    root.add(table).grow();

                    subTable = new Table();
                    subTable.setBackground(skin.getDrawable("table-bg"));
                    subTable.setColor(43 / 255f, 70 / 255f, 109 / 255f, 1);
                    subTable.pad(10 * density);
                    table.add(subTable);
                    
                    label = new Label("Music: The Jazz Piano by bensound.com", skin);
                    subTable.add(label);

                    subTable.row();
                    label = new Label("Tile Jigsaw UI: Raymond \"Raeleus\" Buckley", skin);
                    subTable.add(label);

                    subTable.row();
                    label = new Label("Tile Jigsaw created by MPuzzler", skin);
                    subTable.add(label);

                    break;
                case SETUP:
                    table = generateMenuTable();
                    root.add(table).growY();
                    
                    table = new Table();
                    table.pad(5);
                    root.add(table).grow();
                    
                    subTable = new Table();
                    subTable.setBackground(skin.getDrawable("table-bg"));
                    subTable.setColor(43 / 255f, 70 / 255f, 109 / 255f, 1);
                    subTable.pad(10 * density);
                    table.add(subTable).expand();
                    
                    subTable.defaults().space(15 * density);
                    label = new Label("Max number of tiles in a piece", skin, "menu");
                    subTable.add(label).right();
                    
                    Button button = new Button(skin, "minus");
                    subTable.add(button);
                    
                    TextField textField = new TextField("1", skin);
                    textField.setAlignment(Align.center);
                    subTable.add(textField).width(textField.getStyle().background.getMinWidth());
                    
                    button = new Button(skin, "plus");
                    subTable.add(button);
                    
                    subTable.row();
                    label = new Label("Number of rows in puzzle", skin, "menu");
                    subTable.add(label).right();
                    
                    button = new Button(skin, "minus");
                    subTable.add(button);
                    
                    textField = new TextField("1", skin);
                    textField.setAlignment(Align.center);
                    subTable.add(textField).width(textField.getStyle().background.getMinWidth());
                    
                    button = new Button(skin, "plus");
                    subTable.add(button);
                    
                    subTable.row();
                    label = new Label("Number of columns in puzzle", skin, "menu");
                    subTable.add(label).right();
                    
                    button = new Button(skin, "minus");
                    subTable.add(button);
                    
                    textField = new TextField("1", skin);
                    textField.setAlignment(Align.center);
                    subTable.add(textField).width(textField.getStyle().background.getMinWidth());
                    
                    button = new Button(skin, "plus");
                    subTable.add(button);
                    
                    table.row();
                    TextButton textButton = new TextButton("Start", skin, "start");
                    table.add(textButton).expand();
                    textButton.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                            transition(Scene.GAME);
                        }
                    });
                    
                    break;
                case GAME:
                    label = new Label("Level 1", skin, "start");
                    root.add(label).expand().top().left().pad(10);
                    
                    final Table gameMenuTable = new Table();
                    gameMenuTable.setBackground(skin.getDrawable("white"));
                    gameMenuTable.setColor(new Color(6 / 255.0f, 11 / 255.0f, 17 / 255.0f, 1));
                    gameMenuTable.pad(2 * density);
                    root.add(gameMenuTable).expandY().top().pad(10);
                    
                    ImageButton imageButton = new ImageButton(skin, "game-puzzles");
                    gameMenuTable.add(imageButton);
                    final ChangeListener changeListener = new ChangeListener() {
                        @Override
                        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                            gameMenuTable.clear();

                            gameMenuTable.defaults().space(2 * density);
                            ImageButton imageButton = new ImageButton(skin, "game-close");
                            imageButton.row();
                            Label label = new Label("Close", skin);
                            imageButton.add(label);
                            gameMenuTable.add(imageButton);
                            final ChangeListener changeListener = this;
                            imageButton.addListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                                    gameMenuTable.clear();
                                    
                                    ImageButton imageButton = new ImageButton(skin, "game-puzzles");
                                    gameMenuTable.add(imageButton);
                                    imageButton.addListener(changeListener);
                                }
                            });
                            
                            gameMenuTable.row();
                            imageButton = new ImageButton(skin, "game-scatter");
                            imageButton.row();
                            label = new Label("Scatter", skin);
                            imageButton.add(label);
                            gameMenuTable.add(imageButton);
                            
                            gameMenuTable.row();
                            imageButton = new ImageButton(skin, "game-colors");
                            imageButton.row();
                            label = new Label("Colors", skin);
                            imageButton.add(label);
                            gameMenuTable.add(imageButton);
                            
                            gameMenuTable.row();
                            imageButton = new ImageButton(skin, "game-hints");
                            imageButton.row();
                            label = new Label("Hints", skin);
                            imageButton.add(label);
                            gameMenuTable.add(imageButton);
                            imageButton.addListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                                    showHintsDialog();
                                }
                            });
                            
                            gameMenuTable.row();
                            imageButton = new ImageButton(skin, "game-music");
                            imageButton.row();
                            label = new Label("Music", skin);
                            imageButton.add(label);
                            gameMenuTable.add(imageButton);
                            
                            gameMenuTable.row();
                            imageButton = new ImageButton(skin, "game-home");
                            imageButton.row();
                            label = new Label("Home", skin);
                            imageButton.add(label);
                            gameMenuTable.add(imageButton);
                            imageButton.addListener(new ChangeListener() {
                                @Override
                                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                                    showHomeDialog();
                                }
                            });
                        }
                    };
                    imageButton.addListener(changeListener);
                    
                    break;
            }
            
            root.setColor(root.getColor().r, root.getColor().g, root.getColor().b, 0);
            root.addAction(Actions.fadeIn(.25f));
        }
    }
    
    private Table generateMenuTable() {
        Table table = new Table();
        table.setBackground(skin.getDrawable("white"));
        table.setColor(new Color(6 / 255.0f, 11 / 255.0f, 17 / 255.0f, 1));
        table.pad(2);

        table.defaults().space(2).grow();
        ImageButton imageButton = new ImageButton(skin, "menu-puzzles");
        imageButton.row();
        Label label = new Label("Puzzles", skin, "menu");
        imageButton.add(label);
        table.add(imageButton);
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                transition(Scene.MENU);
            }
        });

        table.row();
        imageButton = new ImageButton(skin, "menu-settings");
        imageButton.row();
        label = new Label("Settings", skin, "menu");
        imageButton.add(label);
        table.add(imageButton);
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                transition(Scene.SETTINGS);
            }
        });

        table.row();
        imageButton = new ImageButton(skin, "menu-review");
        imageButton.row();
        label = new Label("Review", skin, "menu");
        imageButton.add(label);
        table.add(imageButton);

        table.row();
        imageButton = new ImageButton(skin, "menu-credits");
        imageButton.row();
        label = new Label("Credits", skin, "menu");
        imageButton.add(label);
        table.add(imageButton);
        imageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                transition(Scene.CREDITS);
            }
        });
        
        return table;
    }

    private void showHintsDialog() {
        Dialog dialog = new Dialog("", skin);
        dialog.getContentTable().pad(50 * density).padTop(25 * density).padBottom(25 * density);
        
        dialog.text("No more hints left", skin.get("dialog", Label.LabelStyle.class));
        
        TextButton textButton = new TextButton("Get some", skin, "dialog");
        dialog.button(textButton, true);
        
        textButton = new TextButton("I'm fine", skin, "dialog");
        dialog.button(textButton, false);
        
        dialog.show(stage);
    }
    
    private void showHomeDialog() {
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    transition(Scene.MENU);
                }
            }
            
        };
        dialog.getContentTable().pad(50 * density).padTop(25 * density).padBottom(25 * density);
        
        dialog.text("Going home, game will be lost, proceed?", skin.get("dialog", Label.LabelStyle.class));
        
        TextButton textButton = new TextButton("Yes", skin, "dialog");
        dialog.button(textButton, true);
        
        textButton = new TextButton("Stay", skin, "dialog");
        dialog.button(textButton, false);
        
        dialog.show(stage);
    }
     
    private void showExitDialog() {
        Dialog dialog = new Dialog("", skin);
        dialog.getContentTable().pad(50 * density).padTop(25 * density).padBottom(25 * density);
        
        dialog.text("Exiting, save the game?", skin.get("dialog", Label.LabelStyle.class));
        
        TextButton textButton = new TextButton("Yes", skin, "dialog");
        dialog.button(textButton, 0);
        
        textButton = new TextButton("No", skin, "dialog");
        dialog.button(textButton, 1);
        
        textButton = new TextButton("Stay", skin, "dialog");
        dialog.button(textButton, 2);
        
        dialog.show(stage);
    }
    
    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        manager.dispose();
    }
}
