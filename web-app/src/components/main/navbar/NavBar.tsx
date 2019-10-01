import React, {FunctionComponent} from 'react'

import {NavFolder} 		from './NavFolder/NavFolder';
import NavNode 			from '../../../model/NavNode';
import {Notification} 	from '../../../model/notification/Notification';

type Props = {
	navNodes: NavNode[]
	notifications: Notification[]
};

const NavBar: FunctionComponent<Props> = props => (

	<nav>
		<div className="navigation-folders">
			{props.navNodes.map(node => <NavFolder key={node.id} data={node}/>)}
		</div>
		<div className="nav-right-content">
			<div className="nav-menu-button" onClick={() => {
				const rightPanel = document.getElementById("right-panel");
				
				if (rightPanel !== null) rightPanel.style.right = '0';
			}}>
				<i className={props.notifications.length > 0 ? "fas fa-bell" : "far fa-bell"} style={{ lineHeight : '40px' }}/>
			</div>
		</div>
	</nav>
);

export default NavBar;