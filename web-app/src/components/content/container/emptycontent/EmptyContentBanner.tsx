import React from 'react';

export const EmptyContentBanner: React.SFC = () => (
	<div className="empty-content-banner">
		<div className="empty-content-banner-icon">
			<i className="fas fa-folder"/>
		</div>
		<div>
			<span>Empty folder</span>
		</div>
	</div>
);