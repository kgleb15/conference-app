import App from './App';
import SubmitPage from './SubmitPage';

export default function Root() {
    const Page = window.location.pathname === '/submit' ? SubmitPage : App;
    return <Page />;
}
