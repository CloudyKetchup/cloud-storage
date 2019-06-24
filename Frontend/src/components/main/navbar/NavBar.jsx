import React from 'react'

const NavBar = props => (
	
	<nav>
		<div>
			
		</div>
		<div className="nav-right-content">
			<div className="nav-menu-button" onClick={() => document.getElementById("right-panel").style.right = 0}>
				<i className="fas fa-bars" style={{ lineHeight : '40px' }}/>
			</div>
		</div>
	</nav>
);	

export default NavBar;