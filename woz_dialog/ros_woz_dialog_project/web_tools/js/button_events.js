var expanded = false;
var expandedButtonId = 0;

function expand(clickedButton)
{
  
  var id = clickedButton.id;
  var query = "g:not(#"+id+").group_button"


  if(expanded)
  {
  	if(expandedButtonId == id)
  	{
  	  var elementsToUnBlur = document.querySelectorAll(query);

  	  for(var i=0; i<elementsToUnBlur.length; i++)
	  {
	  	var button = elementsToUnBlur[i];
	  	button.classList.remove('chilled')
	  }

	  unexpand_inner(id);

	  expanded = false;
	  expandedButtonId = 0;




  	}else
  	  return;
  }else{

  //Glass out all other elements not belonging to the clicked button
  var elementsToBlur = document.querySelectorAll(query);

  for(var i=0; i<elementsToBlur.length; i++)
  {
  	var button = elementsToBlur[i];
  	button.classList.add('chilled')
  }


  //Expand the the option buttons

  expand_inner(id);

  expanded = true;
  expandedButtonId = id;
  }


}

function expand_inner(id)
{
	var query = "."+id+"_joint";
	var elementsToSee = document.querySelectorAll(query);

	for(var i=0; i < elementsToSee.length; i++)
	{
		var joint = elementsToSee[i];
		console.log(joint.id);
		joint.style.display = "inline";
	}

}

function unexpand_inner(id)
{

	var query = "."+id+"_joint";
	var elementsToSee = document.querySelectorAll(query);

	for(var i=0; i < elementsToSee.length; i++)
	{
		var joint = elementsToSee[i];
		console.log(joint.id);
		joint.style.display = "none";
	}

}