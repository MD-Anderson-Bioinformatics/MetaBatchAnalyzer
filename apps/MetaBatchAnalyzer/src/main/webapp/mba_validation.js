
ko.validation.rules['minArrayLength'] = 
{
	validator: function (obj, params)
	{
		return obj.length >= params.minLength;
	},
	message: "Array does not meet minimum length requirements"
};

//Must call registerExtenders() or there will be no validation.
//It won't throw any errors either, it will just be ignored
ko.validation.registerExtenders();