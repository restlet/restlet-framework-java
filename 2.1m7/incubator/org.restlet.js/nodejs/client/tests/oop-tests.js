function copyElements(obj1, obj2) {
	for (var elt in obj1) {
		//console.log("copy "+elt+" ("+typeof obj1[elt]+")");
		if (elt!="initialize" || typeof obj1[elt] != "function") {
			//console.log("  -> ok");
			obj2[elt] = obj1[elt];
		} else {
			obj2["_"+elt] = obj1[elt];
		}
	}
}

var Class = function() {
	var parent = null;
	var content = null;
	if (arguments.length==1) {
		content = arguments[0];
	} else if (arguments.length==2) {
		parent = arguments[0];
		content = arguments[1];
	}
	
	var clazz = function() {
		//console.log("clazz.initializeExtend = "+clazz.initializeExtend);
		if (clazz.initializeExtend!=null && clazz.initializeExtend==true) {
			return;
		}
		if (content!=null && content["initialize"]!=null) {
			content["initialize"].apply(this, arguments);
		}
	}
	if (parent!=null) {
		copyElements(parent, clazz);
		parent.initializeExtend = true;
		//console.log("> new parent");
		clazz.prototype = new parent();
		//console.log("< new parent");
		clazz.parent = parent.prototype;
		parent.initializeExtend = null;
		copyElements(content, clazz.prototype);
		//console.log("adding call super");
		clazz.prototype["callSuper"] = function() {
			//console.log("clazz.parent.prototype = "+clazz.parent.prototype);
			/*console.log("clazz.parent = "+clazz.parent);
			for(var elt in clazz.parent) {
				console.log("- elt "+elt);
			}*/
			if (clazz.parent["_initialize"]!=null) {
				var superInitialize = clazz.parent["_initialize"];
				/*console.log("superInitialize = "+superInitialize);
				console.log("this = "+this);*/
				superInitialize.apply(this);
			}
		};
		//console.log("added call super");
	} else {
		clazz.prototype = {};
		copyElements(content, clazz.prototype);
	}
	//copyElements(parent, clazz);
	/*console.log("clazz.prototype:");
	for (var elt in clazz.prototype) {
		console.log(" - "+elt);
	}*/

	clazz.extend = function(content) {
		copyElements(content, this);
	};
	return clazz;
};

console.log("####### TESTS ###########");

var TestParentClass = new Class({
	initialize: function() {
		console.log("constructor(TestParentClass)");
	},
	method1: function() {
		console.log("TestParentClass(instance).method1");
	}
});

//console.log("TestParentClass.prototype.callSuper = "+TestParentClass.prototype.callSuper);

TestParentClass.extend({
	method1b: function() {
		console.log("TestClass(static).method1b");
	}
});

var TestClass = new Class(TestParentClass, {
	initialize: function() {
		console.log("constructor(TestClass)");
		for (var elt in this) {
			console.log("elt = "+elt);
		}
		this.callSuper();
	},
	method1: function() {
		console.log("TestClass(instance).method1");
	},
	method2: function() {
		console.log("TestClass(instance).method2");
	}
});

/*console.log("TestClass = "+TestClass);
console.log("TestClass.prototype.callSuper = "+TestClass.prototype.callSuper);
console.log("TestClass.prototype:");
for (var elt in TestClass.prototype) {
	console.log(" - "+elt+":" +TestClass.prototype[elt]);
}*/

TestClass.extend({
	method3: function() {
		console.log("TestClass(static).method3");
	}
});

console.log("> instanciating inst1");
var inst1 = new TestClass();
console.log("< instanciated inst1");
inst1.method1();
inst1.method2();
TestClass.method3();
TestClass.method1b();

console.log("> instanciating inst2");
var inst2 = new TestParentClass();
console.log("< instanciated inst2");
inst2.method1();
TestParentClass.method1b();