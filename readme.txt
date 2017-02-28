README
------

Ce projet fournit un framework simple [1] pour l'execution de programmes
accessibles en tant que ressources REST.

Les ressources se programment comment des classes annotees avec l'API JAX-RS.
Voir un example avec la classe car.tp2.HelloWorldResource.

Pour pouvoir etre prises en compte par le framework, les ressources doivent etre
declarees dans la classe car.tp2.Config, methode addResources. La declaration se
fait en ajoutant une ligne de la forme :

	resources.add( new MaClasseDeResource() )
	
Autant de ressources que necessaire peuvent etre declarees.

Le lancement du framework se fait en invoquant la methode Main de la classe
car.tp2.Starter.

Une fois lancees, les ressources sont accessibles, par exemple via un
navigateur, en chargeant une URL de la forme :

	http://localhost:8080/rest/tp2/_ressource_
	par exemple : http://localhost:8080/rest/tp2/helloworld
	

Lionel Seinturier.
23 juillet 2015.


[1] http://aredko.blogspot.fr/2013/01/going-rest-embedding-jetty-with-spring.html