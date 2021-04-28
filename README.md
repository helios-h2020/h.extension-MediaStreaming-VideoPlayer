# MediaStreaming - VideoPlayer

Repository for the VideoPlayer of Media Streaming Module (T3.3).

### Video Player:
This functionality provides a polyvalent video player built with ExoPlayer. By default, the player uses the url provided in the values/strings.xml file located in the code of your application.

* HLS_URI: `https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8`

All these values can be modified in the `values/strings.xml` file.

To start the Video Player (e.g. using the MediaStreaming App to call this extension) it is necessary to introduce the url:

MediaStreaming App: https://github.com/helios-h2020/h.app-MediaStreaming

<img src="https://raw.githubusercontent.com/helios-h2020/h.app-MediaStreaming-LiveVideoStreaming/master/doc/player.png" alt="VideoPlayer">

### How to use Video Player:
The url of the video can be passed as a string extra with name 'URI'
```
        Intent videoPlayerIntent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        videoPlayerIntent.putExtra("URI", "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8");
        MainActivity.this.startActivity(videoPlayerIntent);
```

### Request permissions
There are non additional permissions needed.

## Multiproject dependencies ##

HELIOS software components are organized into different repositories
so that these components can be developed separately avoiding many
conflicts in code integration. However, the modules also depend on
each other.

### How to configure the dependencies ###

To manage project dependencies developed by the consortium, the approach proposed is to use a private Maven repository with Nexus.

To avoid clone all dependencies projects in local, to compile the "father" project. Otherwise, a developer should have all the projects locally to
be able to compile. Using Nexus, the dependencies are located in a remote repository, available to compile, as described in the next section.
Also to improve the automation for deploy, versioning and distribution of the project.

### How to use the HELIOS Nexus ###

Similar to other dependencies available in Maven Central, Google or others repositories. In this case we specify the Nexus
repository provided by Atos: `https://builder.helios-social.eu/repository/helios-repository/`

This URL makes the project dependencies available.

To access, we simply need credentials, that we will define locally in the variables `heliosUser` and `heliosPassword`.

The `build.gradle` of the project define the Nexus repository and the credential variables in this way:

```
repositories {
        ...
        maven {
            url "https://builder.helios-social.eu/repository/helios-repository/"
            credentials {
                username = heliosUser
                password = heliosPassword
            }
        }
    }
```

And the variables of Nexus's credentials are stored locally at `~/.gradle/gradle.properties`:

```
heliosUser=username
heliosPassword=password
```

To request Nexus username and password, contact with: `jordi.hernandezv@atos.net`

### How to deploy a new version of the dependencies ###

Let's say that we want to deploy a new version of the videoplayer project. This project is a dependency of MediaStreaming.
For Continuous Integration we use Jenkins. It deploys the configured projects (e.g., videoplayer) in different jobs,
and the results are libraries packaged like AAR (Android ARchive). These packaged libraries are upload to Nexus and in this way,
they are available to build the projects that depend on them (e.g., MediaStreaming).
In the videoplayer example, Jenkins jobs generate automatically and aar library and store it at the Nexus repository to make it available.

Jenkins is the tool deployed by Atos (WP6 leader) in HELIOS to automate the generation of APKs, joining all the project modules.
Due to the need of managing the dependencies, Atos has selected additional tools, as explained in this document.

After pushing a change to the `master` branch, the maintainer can builds the module by means of the job in the Jenkins interface. GitLab repositories are set to protect
the `master` branch push and merge for the partner in charge of its module/project (maintainer).

To request Jenkins username and password, contact with: `jordi.hernandezv@atos.net`

### How to use the dependencies ###

To use the dependency in `build.gradle` of the "father" project, you should specify the last version available in Nexus, related to the last Jenkins's deploy.
For example, to declare the dependency on the videoplayer module and the respective version:

`implementation 'eu.h2020.helios_social.modules.videoplayer:videoplayer:1.0.21'`

For more info review: `https://scm.atosresearch.eu/ari/helios_group/generic-issues/blob/master/multiprojectDependencies.md`

### How to use the dependencies locally ###

If you want to include the .aar file generated as a dependency in the application whitout use Nexus dependencies:

- Go to your application code and create libs folder inside app folder:

<img src="https://raw.githubusercontent.com/helios-h2020/h.app-MediaStreaming/master/doc/libs.PNG" alt="libs folder">

- Open build.gradle at Project level and add flatDir{dirs 'libs'} :

<img src="https://raw.githubusercontent.com/helios-h2020/h.app-MediaStreaming/master/doc/libs_gradle.PNG" alt="Project build.gradle">

```
allprojects {
   repositories {
      jcenter()
      flatDir {
        dirs 'libs'
      }
   }
}
```

- Open build.gradle at app level and add .aar file:

<img src="https://raw.githubusercontent.com/helios-h2020/h.app-MediaStreaming/master/doc/gradle_app.PNG" alt="app build.gradle">

```
dependencies {
     compile(name:'file_name', ext:'aar')
}
```