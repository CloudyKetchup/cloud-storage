import React from 'react'

const Nav = props => {
	const folderInfo = props.folderInfo;
	return (
		<nav>
			<div className="folder-info">
				<div>
					<span>
						{folderInfo !== undefined ? folderInfo.name : ''}
					</span>
				</div>
				<div>
					<span>
						{props.folderElements} Elements
					</span>
				</div>
				<div>
					<span>
						{folderInfo !== undefined ? folderInfo.path : ''}
					</span>
				</div>
				<div>
					<span>
						{folderInfo !== undefined ? folderInfo.size : ''} Mb
					</span>
				</div>
			</div>
			<div></div>
		</nav>
	);	
};

export default Nav;