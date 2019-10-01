import React from 'react'

const PrevFolderButton = props => {

	const style = {
		display : props.rootOpened ? 'none' : 'block',
		position : 'fixed',
		zIndex : 999,
		padding : 15,
		paddingLeft : 10
	}

	return (
		<button className="prev-button" onClick={props.whenClicked} style={style}>
			<i className="fas fa-chevron-left"></i>
		</button>	
	);
};

export default PrevFolderButton;